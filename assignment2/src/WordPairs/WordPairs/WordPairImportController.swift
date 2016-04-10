//
//  WordPairImportController.swift
//  WordPairs
//
//  Created by Samuel Jackson on 30/03/2016.
//  Copyright © 2016 Samuel Jackson. All rights reserved.
//

import UIKit
import CoreData

class WordPairImportController: UIViewController {
    
    var wordPairs = [WordPhrasePair]()
    
    @IBOutlet weak var importURL: UITextField!
    @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        activityIndicator.hidden = true
    }
    
    /* Handle when the user clicks to import a URL */
    @IBAction func clickImport(sender: AnyObject) {
        // validate to check if a URL has been entered
        if (importURL.text!.isEmpty) {
            let message = "Please Enter a URL"
            MessageHelper.showValidationMessage(message, controller: self)
        } else {
            // update interface and load from the URL
            activityIndicator.hidden = false
            activityIndicator.startAnimating()
            importPairsFromURL()
        }
    }
    
    
    /* Function to import data from the URL.
    
    This handles the getting the JSON data using a seperate task
    */
    func importPairsFromURL() {
        let urlString = importURL.text!
        if let requestURL: NSURL = NSURL(string: urlString) {
            let urlRequest: NSMutableURLRequest = NSMutableURLRequest(URL: requestURL)
            let session = NSURLSession.sharedSession()
            
            let task = session.dataTaskWithRequest(urlRequest) {
                (data, response, error) -> Void in
                self.handleHTTPResponse(response, dataFromResponse: data)
            }
            task.resume()
        } else {
            self.handleImportFailure()
        }
    }
    
    
    /* Handle the HTTP response from a URL 
    
    This checks the response from the URL is good and then parses the JSON data
    */
    func handleHTTPResponse(response: AnyObject?, dataFromResponse data: NSData?) {
        if let httpResponse = response as? NSHTTPURLResponse {
            let statusCode = httpResponse.statusCode
            
            if (statusCode == 200) {
                self.parseJSONResponse(data)
                self.handleImportSuccess()
            } else {
                self.handleImportFailure()
            }
        } else {
            self.handleImportFailure()
        }
    }
    
    /* Parse the JSON response from a URL into a list of word pairs */
    func parseJSONResponse(data: NSData?) {
        do{
            let json = try NSJSONSerialization.JSONObjectWithData(data!, options:.AllowFragments)
            
            if let wordPairsData = json["wordpairs"] as? [[String: AnyObject]] {
                for pair in wordPairsData {
                    let wordPair = parseWordPair(pair)
                    wordPairs.append(wordPair)
                }
            }
        }catch {
            handleImportFailure()
        }
    }
    
    /* Parse a single word pair from the JSON response
    
    and add it to the Core Data model
    */
    func parseWordPair(pair: [String: AnyObject]) -> WordPhrasePair {
        // get data from JSON object
        let nativeWord = pair["wordPhraseOne"] as? String
        let foreignWord = pair["wordPhraseTwo"] as? String
        let note = pair["note"] as? String
        let type = pair["type"] as? String
        
        // create new core data object
        let managedObjectContext = (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext
        let wordPair = NSEntityDescription.insertNewObjectForEntityForName("WordPhrasePair", inManagedObjectContext: managedObjectContext) as? WordPhrasePair
        
        // fill in the data from a new word pair
        wordPair?.native = nativeWord
        wordPair?.foreign = foreignWord
        wordPair?.type = type?.uppercaseFirst
        wordPair?.note = note
        wordPair?.timeAdded = parseDateTimeString(pair["timeAdded"] as? String)

        // save to Core Data
        do {
            try managedObjectContext.save()
        } catch {
            handleImportFailure()
            fatalError("Failed to save new word phrase: \(error)")
        }
        
        return wordPair!
    }
    
    /* Helper function to convert a date time string to a NSDate object

    Note that dates are expected to be in the format yyyy-MM-dd hh:mm:ss
    */
    func parseDateTimeString(timeStringToParse: String?) -> NSDate {
        if let timeString = timeStringToParse as String! {
            let dateFormatter = NSDateFormatter()
            dateFormatter.dateFormat = "yyyy-MM-dd hh:mm:ss"
            return dateFormatter.dateFromString(timeString)!
        } else {
            return NSDate()
        }
    }
    
    /* Handle when importing from the URL was successful 
    
    This will segue back to the previous screen
    */
    func handleImportSuccess() {
        NSOperationQueue.mainQueue().addOperationWithBlock {
            MessageHelper.alertSuccess("Data Imported Successfully!", controller: self, handler: { (alert :UIAlertAction!) in
                self.performSegueWithIdentifier("ImportSuccess", sender: nil);
            })
            self.activityIndicator.stopAnimating()
            self.activityIndicator.hidden = true
        }
    }
    
    /* Handle when importing from the URL failed for some reason */
    func handleImportFailure() {
        NSOperationQueue.mainQueue().addOperationWithBlock {
            MessageHelper.alertFailure("Sorry. Could not import from that URL.", controller: self)
            self.activityIndicator.stopAnimating()
            self.activityIndicator.hidden = true
        }
    }
}