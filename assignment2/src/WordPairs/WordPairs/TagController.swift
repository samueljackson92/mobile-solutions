//
//  TagController.swift
//  WordPairs
//
//  Created by Samuel Jackson on 29/03/2016.
//  Copyright © 2016 Samuel Jackson. All rights reserved.
//

import UIKit
import CoreData

class TagController: UITableViewController, UISearchResultsUpdating {

    let searchController = UISearchController(searchResultsController: nil)
    let managedObjectContext = (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext
    
    // containers for tags and filtered tags in core data
    var tags = [Tag]()
    var filteredTags = [Tag]()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.title = "Tags"
        navigationItem.leftBarButtonItem = editButtonItem()
        
        //setup search controller
        searchController.searchResultsUpdater = self
        searchController.hidesNavigationBarDuringPresentation = false
        searchController.dimsBackgroundDuringPresentation = false
        searchController.searchBar.sizeToFit()
        searchController.searchBar.placeholder = "Search Tags"
        self.tableView.tableHeaderView = searchController.searchBar
        loadTags()
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        self.searchController.searchBar.hidden = false
        tableView.reloadData()
    }
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if searchController.active && searchController.searchBar.text != "" {
            return filteredTags.count
        }
        return tags.count
    }
    
    // Override to support showing core data objects in the table view.
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCellWithIdentifier("tagCell")
        let tag : Tag
        
        // when searching the cell returned is nil
        // attempting to extract this in the return statement throws an exception
        // this code provides a work around
        // see stackoverflow #25461545
        if !(cell != nil) {
            cell = UITableViewCell(style: UITableViewCellStyle.Value1, reuseIdentifier: "Tag")
        }
        
        tag = getTagFromContext(indexPath)
        
        let wordPairCount = tag.wordPairs!.count
        cell?.textLabel!.text = tag.name
        cell?.detailTextLabel!.text = "Phrases \(wordPairCount)"
        
        if wordPairCount == 0 {
            cell?.accessoryType = UITableViewCellAccessoryType.None
            cell?.selectionStyle = UITableViewCellSelectionStyle.None
        } else {
            cell?.accessoryType = UITableViewCellAccessoryType.DisclosureIndicator
            cell?.selectionStyle = UITableViewCellSelectionStyle.Gray
        }
        
        return cell!
    }
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        let cell = tableView.cellForRowAtIndexPath(indexPath)
        if editing {
            self.performSegueWithIdentifier("ViewTagDetail", sender: cell);
        } else if tags[indexPath.row].wordPairs?.count > 0 {
            self.performSegueWithIdentifier("ViewWordsForTag", sender: cell)
        }
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if segue.identifier == "ViewTagDetail" && editing {
            // handle the case where we're editing a tag
            segueToViewTagDetail(segue, sender: sender)
        } else if segue.identifier == "ViewWordsForTag" {
            // handle the case where we're viewing a tag
            segueToViewWordForTag(segue, sender: sender)
        }
    }
    
    /* Prepare segue to view add/edit a tag */
    func segueToViewTagDetail(segue: UIStoryboardSegue, sender: AnyObject?) {
        let navController = segue.destinationViewController as! UINavigationController
        let tagDetail = navController.viewControllers.first as! TagDetail
        
        if let selectedCell = sender as? UITableViewCell {
            let indexPath = tableView.indexPathForCell(selectedCell)!
            let tag = tags[indexPath.row]
            tagDetail.tag = tag
        }
    }
    
    /* Prepare segue to view the words associated with a tag */
    func segueToViewWordForTag(segue: UIStoryboardSegue, sender: AnyObject?) {
        let tagWordPairController = segue.destinationViewController as! TagWordPairViewController
        
        if let selectedCell = sender as? UITableViewCell {
            let indexPath = tableView.indexPathForCell(selectedCell)!
            let tag = getTagFromContext(indexPath)
            let name = tag.name!
            
            tagWordPairController.title = "Word Pairs for \(name)"
            tagWordPairController.wordPairs = tag.getAllWordPairsForTag()
            self.searchController.searchBar.hidden = true
        }
    }
    
    func updateSearchResultsForSearchController(searchController: UISearchController){
        filterContentForSearchText(searchController.searchBar.text!)
    }
    
    /* Filter function for search for tags by name */
    func filterContentForSearchText(searchText: String) {
        filteredTags = tags.filter { tag in
                return tag.name!.lowercaseString.containsString(searchText.lowercaseString)
        }
        tableView.reloadData()
    }
    
    // Override to support editing the table view.
    override func tableView(tableView: UITableView, commitEditingStyle editingStyle: UITableViewCellEditingStyle, forRowAtIndexPath indexPath: NSIndexPath) {
        if editingStyle == .Delete {
            // Delete the row from the data source
            deleteTag(indexPath)
        }
    }
    
    /** Action to handle when a user clicks Cancel on the add tags screen */
    @IBAction func cancelTagDetail(segue:UIStoryboardSegue) {
        // if we cancel we should do nothing
        // just close the segue
    }
    
    /** Action to handle when a user clicks Done on the add tags screen */
    @IBAction func saveTagDetail(segue:UIStoryboardSegue) {
        if let tagDetailController = segue.sourceViewController as? TagDetail,
            tag = tagDetailController.tag {
                
            if let selectedIndexPath = tableView.indexPathForSelectedRow {
                //edit the pair by overwriting it
                if isInSearchMode() {
                    filteredTags[selectedIndexPath.row] = tag
                } else {
                    tags[selectedIndexPath.row] = tag
                }

                tableView.reloadRowsAtIndexPaths([selectedIndexPath], withRowAnimation: .None)
            } else {
                //add the new tag to the tag array
                if isInSearchMode() {
                    filteredTags.append(tag)
                } else {
                    tags.append(tag)
                }
                
                //update the tableView
                let indexPath = NSIndexPath(forRow: tags.count-1, inSection: 0)
                tableView.insertRowsAtIndexPaths([indexPath], withRowAnimation: .Automatic)
            }
        }
    }
    
    /** Delete a tag
     
     This removes the object from the core data store, tag list and from the view
     */
    func deleteTag(indexPath: NSIndexPath) {
        do {
            if isInSearchMode() {
                managedObjectContext.deleteObject(filteredTags[indexPath.row] as NSManagedObject)
                let tag = filteredTags[indexPath.row]
                if let tagIndex = tags.indexOf(tag) {
                    filteredTags.removeAtIndex(indexPath.row)
                    tags.removeAtIndex(tagIndex)
                }
            } else {
                managedObjectContext.deleteObject(tags[indexPath.row] as NSManagedObject)
                tags.removeAtIndex(indexPath.row)
            }

            try managedObjectContext.save()
            tableView.deleteRowsAtIndexPaths([indexPath], withRowAnimation: .Fade)
        } catch {
            fatalError("Failed to delete object: \(error)")
        }
    }
    
    /** Fetch all tags from Core Data */
    func loadTags() {
        let tagsFetch = NSFetchRequest(entityName: "Tag")
        do {
            tags = try managedObjectContext.executeFetchRequest(tagsFetch) as! [Tag]
        } catch {
            fatalError("Failed to fetch tags: \(error)")
        }
    }
    
    /** Helper method for getting a tag
     This should return the correct tag regardless of whether a search
     filter is active
     */
    func getTagFromContext(indexPath: NSIndexPath) -> Tag {
        let tag: Tag
        
        if isInSearchMode() {
            tag = filteredTags[indexPath.row]
        } else {
            tag = tags[indexPath.row]
        }
        
        return tag
    }
    
    /** Check whether the interface is currently in search mode */
    func isInSearchMode() -> Bool {
        return searchController.active && searchController.searchBar.text != ""
    }
}
