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
    
    @IBAction func clickImport(sender: AnyObject) {
        if (importURL.text!.isEmpty) {
            let message = "Please Enter a URL"
            MessageHelper.showValidationMessage(message, controller: self)
        } else {
            importPairsFromURL()
        }
    }
    
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
            self.alertImportFailure()
        }
    }
    
    func handleHTTPResponse(response: AnyObject?, dataFromResponse data: NSData?) {
        if let httpResponse = response as? NSHTTPURLResponse {
            let statusCode = httpResponse.statusCode
            
            if (statusCode == 200) {
                self.parseJSONResponse(data)
                self.alertImportSuccess()
            } else {
                self.alertImportFailure()
            }
        } else {
            self.alertImportFailure()
        }
    }
    
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
            print("Error with Json: \(error)")
        }
    }
    
    func parseWordPair(pair: [String: AnyObject]) -> WordPhrasePair {
        let nativeWord = pair["wordPhraseOne"] as? String
        let foreignWord = pair["wordPhraseTwo"] as? String
        let note = pair["note"] as? String
        let type = pair["type"] as? String
        
        let managedObjectContext = (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext
        let wordPair = NSEntityDescription.insertNewObjectForEntityForName("WordPhrasePair", inManagedObjectContext: managedObjectContext) as? WordPhrasePair
        
        wordPair?.native = nativeWord
        wordPair?.foreign = foreignWord
        wordPair?.type = type
        wordPair?.note = note
        wordPair?.timeAdded = NSDate()

        do {
            try managedObjectContext.save()
        } catch {
            fatalError("Failed to save new word phrase: \(error)")
        }
        return wordPair!
    }
    
    func alertImportSuccess() {
        NSOperationQueue.mainQueue().addOperationWithBlock {
            MessageHelper.alertSuccess("Data Imported Successfully!", controller: self, handler: { (alert :UIAlertAction!) in
                self.performSegueWithIdentifier("ImportSuccess", sender: nil);
            })
        }
    }
    
    func alertImportFailure() {
        NSOperationQueue.mainQueue().addOperationWithBlock {
            MessageHelper.alertFailure("Sorry. Could not import from that URL.", controller: self)
        }
    }
}