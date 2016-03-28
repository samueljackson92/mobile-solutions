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
    let managedObjectContext = (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext
    var wordPairs = [WordPhrasePair]()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        navigationItem.leftBarButtonItem = editButtonItem()
        loadWordPairs()
    }
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return wordPairs.count
    }
    
    // Override to support showing core data objects in the table view.
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier("wordPairCell")
        prepareCellForDisplay(cell, cellForRowAtIndexPath: indexPath)
        return cell!
    }
    
    // Override to support editing the table view.
    override func tableView(tableView: UITableView, commitEditingStyle editingStyle: UITableViewCellEditingStyle, forRowAtIndexPath indexPath: NSIndexPath) {
        if editingStyle == .Delete {
            // Delete the row from the data source
            deleteWordPair(indexPath)
        } else if editingStyle == .Insert {
            // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
        }
    }
    
    /** Action to handle when a user clicks Cancel on the add word screen */
    @IBAction func cancelAddWordPair(segue:UIStoryboardSegue) {
        // if we cancel we should do nothing
        // just close the segue
    }

    /** Action to handle when a user clicks Done on the add word screen */
    @IBAction func saveAddWordPair(segue:UIStoryboardSegue) {
        if let addWordPairController = segue.sourceViewController as? AddWordPairViewController {
            if let pair = addWordPairController.pair {
                //add the new word pair to the word pair array
                wordPairs.append(pair)
                
                //update the tableView
                let indexPath = NSIndexPath(forRow: wordPairs.count-1, inSection: 0)
                tableView.insertRowsAtIndexPaths([indexPath], withRowAnimation: .Automatic)
            }
        }
    }
    
    /** Delete a word pair
     
     This removes the object from the core data store, word list and from the view
     */
    func deleteWordPair(indexPath: NSIndexPath) {
        do {
            managedObjectContext.deleteObject(wordPairs[indexPath.row] as NSManagedObject)
            try managedObjectContext.save()
            wordPairs.removeAtIndex(indexPath.row)
            tableView.deleteRowsAtIndexPaths([indexPath], withRowAnimation: .Fade)
        } catch {
            fatalError("Failed to delete object: \(error)")
        }
    }
    
    /** Load all word pairs

    This accesses the core data store and pulls all of the word pairs out into 
    a list.
     */
    func loadWordPairs() {
        let wordPairsFetch = NSFetchRequest(entityName: "WordPhrasePair")
        do {
            wordPairs = try managedObjectContext.executeFetchRequest(wordPairsFetch) as! [WordPhrasePair]
        } catch {
            fatalError("Failed to fetch word pairs: \(error)")
        }
    }
    
    /** Prepare cell by loading the information for a word pair into the cell
     */
    func prepareCellForDisplay(cell:UITableViewCell?, cellForRowAtIndexPath indexPath: NSIndexPath) {
        let pair = wordPairs[indexPath.row]
        let nativeWord = pair.native
        let foreignWord = pair.foreign
        // safely join two optinal strings
        let title = [nativeWord, foreignWord].flatMap{$0}.joinWithSeparator(" - ")
        cell?.textLabel!.text = title
    }
}
