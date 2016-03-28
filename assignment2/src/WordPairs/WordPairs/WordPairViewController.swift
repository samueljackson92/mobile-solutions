//
//  WordPairViewController.swift
//  WordPairs
//
//  Created by Samuel Jackson on 28/03/2016.
//  Copyright © 2016 Samuel Jackson. All rights reserved.
//

import UIKit
import CoreData

class WordPairViewController: UITableViewController, UISearchResultsUpdating  {
    
    let searchController = UISearchController(searchResultsController: nil)
    let managedObjectContext = (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext
    
    var wordPairs = [WordPhrasePair]()
    var filteredWordPairs = [WordPhrasePair]()
    let searchScopeCategories = ["Phrases", "Word Type", "Tags"]
    
    override func viewDidLoad() {
        super.viewDidLoad()
        navigationItem.leftBarButtonItem = editButtonItem()
        
        //setup search controller
        searchController.searchResultsUpdater = self
        searchController.hidesNavigationBarDuringPresentation = false
        searchController.dimsBackgroundDuringPresentation = false
        searchController.searchBar.sizeToFit()
        searchController.searchBar.scopeButtonTitles = searchScopeCategories
        self.tableView.tableHeaderView = searchController.searchBar
        loadWordPairs()
    }
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if searchController.active && searchController.searchBar.text != "" {
            return filteredWordPairs.count
        }
        return wordPairs.count
    }
    
    // Override to support showing core data objects in the table view.
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCellWithIdentifier("wordPairCell")
        let pair : WordPhrasePair
        
        // when searching the cell returned is nil
        // attempting to extract this in the return statement throws an exception
        // this code provides a work around
        // see stackoverflow #25461545
        if !(cell != nil) {
            cell = UITableViewCell(style: UITableViewCellStyle.Value1, reuseIdentifier: "wordPairCell")
        }

        if searchController.active && searchController.searchBar.text != "" {
            pair = filteredWordPairs[indexPath.row]
        } else {
            pair = wordPairs[indexPath.row]
        }
        
        prepareCellForDisplay(cell, wordPair: pair)
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
    
    func filterContentForSearchText(searchText: String, searchScope scope: String = "All") {
        filteredWordPairs = wordPairs.filter { pair in
            switch scope {
                case "Phrases":
                    return pair.native!.lowercaseString.containsString(searchText.lowercaseString) ||
                            pair.foreign!.lowercaseString.containsString(searchText.lowercaseString)
                case "Word Type":
                    return pair.type!.lowercaseString.containsString(searchText.lowercaseString)
                case "Tags":
                    return false
                default:
                    return false
            }
        }
        tableView.reloadData()
    }
    
    func updateSearchResultsForSearchController(searchController: UISearchController) {
        let scope = self.searchScopeCategories[searchController.searchBar.selectedScopeButtonIndex]
        filterContentForSearchText(searchController.searchBar.text!, searchScope: scope)
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
    func prepareCellForDisplay(cell:UITableViewCell?, wordPair pair: WordPhrasePair) {
        let nativeWord = pair.native
        let foreignWord = pair.foreign
        // safely join two optinal strings
        let title = [nativeWord, foreignWord].flatMap{$0}.joinWithSeparator(" - ")
        cell?.textLabel!.text = title
        cell?.detailTextLabel!.text = pair.type
    }
}
