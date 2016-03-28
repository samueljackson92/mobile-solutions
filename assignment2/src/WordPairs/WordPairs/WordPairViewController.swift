//
//  WordPairViewController.swift
//  WordPairs
//
//  Created by Samuel Jackson on 28/03/2016.
//  Copyright © 2016 Samuel Jackson. All rights reserved.
//

import UIKit
import CoreData

class WordPairViewController: UITableViewController {
    
    var wordPairs = [WordPhrasePair]()
    
    override func viewDidLoad() {
        super.viewDidLoad()

        let managedObjectContext = (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext
        let wordPairsFetch = NSFetchRequest(entityName: "WordPhrasePair")
        
        do {
            wordPairs = try managedObjectContext.executeFetchRequest(wordPairsFetch) as! [WordPhrasePair]
        } catch {
            fatalError("Failed to fetch word pairs: \(error)")
        }
    }
    
    @IBAction func addWordPair(sender: AnyObject) {
        
    }
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return wordPairs.count
    }
    
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier("wordPairCell")
        
        let pair = wordPairs[indexPath.row]
        let nativeWord = pair.native
        let foreignWord = pair.foreign
        // safely join two optinal strings
        let title = [nativeWord, foreignWord].flatMap{$0}.joinWithSeparator(" - ")
        cell!.textLabel!.text = title
        
        return cell!
    }
    
    @IBAction func cancelAddWordPair(segue:UIStoryboardSegue) {
        // if we cancel we should do nothing
        // just close the segue
    }
    
    @IBAction func saveAddWordPair(segue:UIStoryboardSegue) {
        if let addWordPairController = segue.sourceViewController as? AddWordPairViewController {
            //add the new player to the players array
            if let pair = addWordPairController.pair {
                wordPairs.append(pair)

                //update the tableView
                let indexPath = NSIndexPath(forRow: wordPairs.count-1, inSection: 0)
                tableView.insertRowsAtIndexPaths([indexPath], withRowAnimation: .Automatic)
            }
        }
    }
}
