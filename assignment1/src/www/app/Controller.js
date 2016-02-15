var Conference = Conference || {};

Conference.controller = (function ($, dataContext, document) {
    "use strict";

    var position = null;
    var infoWindow = null;
    var map = null;
    var mapDisplayed = false;
    var currentMapWidth = 0;
    var currentMapHeight = 0;
    var sessionsListSelector = "#sessions-list-content";
    var noSessionsCachedMsg = "<div>Your sessions list is empty.</div>";
    var databaseNotInitialisedMsg = "<div>Your browser does not support local databases.</div>";

    var TECHNICAL_SESSION = "Technical",
        SESSIONS_LIST_PAGE_ID = "sessions",
        MAP_PAGE = "map";

    // This changes the behaviour of the anchor <a> link
    // so that when we click an anchor link we change page without
    // updating the browser's history stack (changeHash: false).
    // We also don't want the usual page transition effect but
    // rather to have no transition (i.e. tabbed behaviour)
    var initialisePage = function (event) {
        change_page_back_history();
    };

    var onPageChange = function (event, data) {
        // Find the id of the page
        var toPageId = data.toPage.attr("id");

        // If we're about to display the map tab (page) then
        // if not already displayed then display, else if
        // displayed and window dimensions changed then redisplay
        // with new dimensions
        switch (toPageId) {
            case SESSIONS_LIST_PAGE_ID:
                dataContext.processSessionsList(renderSessionsList);
                break;
            case MAP_PAGE:
                if (!mapDisplayed || (currentMapWidth != get_map_width() ||
                    currentMapHeight != get_map_height())) {
                    deal_with_geolocation();
                }
                break;
        }
    };

    
    
    var renderSessionItem = function(obj) { 
        // render a single session item as HTML
        var content = '<li>';
        content += '<a href="" class="ui-btn ui-btn-icon-right ui-icon-carat-r">';
        content += '<div class="session-list-item">';
        content += '<h3>' + obj.title + '</h3>';
        content += '<div>';
        content += '<h6>' + obj.type + '</h6>';
        content += '<h6>' + obj.starttime + ' - ' + obj.endtime  + '</h6>';
        content += '</div>';
        content += '</div>';
        content += '</a>';
        content += '</li>';
        return content;
    }
    
    var renderSessionsList = function (sessionsList) {
        // This is where you do the work to build the HTML ul list
        // based on the data you've received from DataContext.js (it
        // calls this method with the list of data)
        // Here are some things you need to do:
        // o Obtain a reference to #sessions-list-content element
        // o If the sessionsList is empty append a div with an error message to the page
        // o Create the <ul> element using jQuery commands and append to the sessions section
        // o Loop through the sessionsList data building up an appropriate set of <li>
        // elements. See how we do this in the worksheet version that hard-codes the
        // session data in index.html
        // o Append the list items to the <ul> element created earlier. Hint: building
        // up an array and then converting to a string with join before appending
        // would help.
        // o You will need to refresh JQM by calling listview function
        
        var sessionsContent = $('#sessions-list-content');
        var sessionsHTML = '';
        
        // handle when we have no sessions
        if (sessionsList.length === 0) {
            sessionsHTML += "<div>No Items</div>";
            sessionsContent.html(sessionsHTML);
        } else { 
            sessionsHTML += '<ul data-role="listview" data-filter="true" class="ui-listview">'
           
            // convert each item to a string HTML list item 
            var sessionsListHTML = sessionsList.map(renderSessionItem);  
            sessionsHTML += sessionsListHTML.join(''); 
            sessionsHTML += "</ul>"; 
            sessionsContent.html(sessionsHTML);
            
            // must trigger a create here to let JQM know new widgets are to be created
            sessionsContent.trigger('create');
            
            // add filter search bar after loading content
            $('.ui-listview').filterable({
                filterPlaceholder: "Search for sessions..."
            });
        } 
    };

    var noDataDisplay = function (event, data) {
        var view = $(sessionsListSelector);
        view.empty();
        $(databaseNotInitialisedMsg).appendTo(view);
    }

    var change_page_back_history = function () {
        $('a[data-role="tab"]').each(function () {
            var anchor = $(this);
            anchor.bind("click", function () {
                $.mobile.changePage(anchor.attr("href"), { // Go to the URL
                    transition: "none",
                    changeHash: false
                });
                return false;
            });
        });
    };

    var deal_with_geolocation = function () {
        var phoneGapApp = (document.URL.indexOf('http://') === -1 && document.URL.indexOf('https://') === -1 );
        if (navigator.userAgent.match(/(iPhone|iPod|iPad|Android|BlackBerry)/)) {
            // Running on a mobile. Will have to add to this list for other mobiles.
            // We need the above because the deviceready event is a phonegap event and
            // if we have access to PhoneGap we want to wait until it is ready before
            // initialising geolocation services
            if (phoneGapApp) {
                document.addEventListener("deviceready", initiate_geolocation, false);
            }
            else {
                initiate_geolocation(); // Directly from the mobile browser
            }
        } else {
            initiate_geolocation(); // Directly from the browser
        }
    };

    var initiate_geolocation = function () {

        // Do we have built-in support for geolocation (either native browser or phonegap)?
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(handle_geolocation_query, handle_errors);
        }
        else {
            // We don't so let's try a polyfill
            yqlgeo.get('visitor', normalize_yql_response);
        }
    };

    var handle_errors = function (error) {
        switch (error.code) {
            case error.PERMISSION_DENIED:
                alert("user did not share geolocation data");
                break;

            case error.POSITION_UNAVAILABLE:
                alert("could not detect current position");
                break;

            case error.TIMEOUT:
                alert("retrieving position timed out");
                break;

            default:
                alert("unknown error");
                break;
        }
    };

    var normalize_yql_response = function (response) {
        if (response.error) {
            var error = { code: 0 };
            handle_errors(error);
            return;
        }

        position = {
            coords: {
                latitude: response.place.centroid.latitude,
                longitude: response.place.centroid.longitude
            },
            address: {
                city: response.place.locality2.content,
                region: response.place.admin1.content,
                country: response.place.country.content
            }
        };

    };

    var create_google_map = function(pos) { 
        position = pos;
        
        //create a dynamic google map 
        map = new google.maps.Map(document.getElementById('map-canvas'), {
            center: {lat: position.coords.latitude, lng: position.coords.longitude},
            zoom: 15
        });

        // hook into resize events. This makes the map responsive
        google.maps.event.addDomListener(window, "resize", function() {
            var center = map.getCenter();
            google.maps.event.trigger(map, "resize");
            map.setCenter(center); 
        });
        
        mapDisplayed = true;
    };

    var create_marker = function (venue) { 
        // create a pin marker on the map
         var marker = new google.maps.Marker({
            position: {lat: parseFloat(venue.latitude), lng: parseFloat(venue.longitude)},
            map: map,
            title: venue.name
         });
        
         // Add a click listener to the marker to show content
         marker.addListener('click', function() {
            // close any open info windows
            if (infoWindow) {
                infoWindow.close();
            }
            
            // create a new info window
            infoWindow = new google.maps.InfoWindow({
                content: venue.name
            });
            
            //display the window
            infoWindow.open(map, marker);
         });
    };

    var handle_geolocation_query = function (pos) {
        create_google_map(pos); 
        
        // load venues and plot markers
        dataContext.processLocationsList(function(venues) {
            venues.forEach(create_marker); 
        });
    };

    var init = function () {
        // The pagechange event is fired every time we switch pages or display a page
        // for the first time.
        var d = $(document);
        var databaseInitialised = dataContext.init();
        if (!databaseInitialised) {
            d.on('pagechange', $(document), noDataDisplay);
        }
       
        // The pagechange event is fired every time we switch pages or display a page
        // for the first time.
        d.on('pagechange', $(document), onPageChange);
        // The pageinit event is fired when jQM loads a new page for the first time into the
        // Document Object Model (DOM). When this happens we want the initialisePage function
        // to be called.
        d.on('pageinit', $(document), initialisePage);
    };


    // Provides a hash of functions that we return to external code so that they
    // know which functions they can call. In this case just init.
    var pub = {
        init: init
    };

    return pub;
}(jQuery, Conference.dataContext, document));

// Called when jQuery Mobile is loaded and ready to use.
$(document).on('mobileinit', $(document), function () {
    Conference.controller.init();
});


