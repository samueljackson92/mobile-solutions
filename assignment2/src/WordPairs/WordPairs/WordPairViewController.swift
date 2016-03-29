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
        searchController.searchBar.placeholder = "Search Word Pairs"
        self.tableView.tableHeaderView = searchController.searchBar
        loadWordPairs()
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        tableView.reloadData()
    }
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if isInSearchMode() {
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

        pair = getWordPairFromContext(indexPath)
        prepareCellForDisplay(cell, wordPair: pair)
        return cell!
    }
    
    // Override to support editing the table view.
    override func tableView(tableView: UITableView, commitEditingStyle editingStyle: UITableViewCellEditingStyle, forRowAtIndexPath indexPath: NSIndexPath) {
        if editingStyle == .Delete {
            // Delete the row from the data source
            deleteWordPair(indexPath)
        }
    }
    
    /** Action to handle when a user clicks Cancel on the add word screen */
    @IBAction func cancelAddWordPair(segue:UIStoryboardSegue) {
        // if we cancel we should do nothing
        // just close the segue
    }

    /** Action to handle when a user clicks Done on the add word screen */
    @IBAction func saveAddWordPair(segue:UIStoryboardSegue) {
        if let addWordPairController = segue.sourceViewController as? AddWordPairViewController,
            pair = addWordPairController.pair {
            
            if let selectedIndexPath = tableView.indexPathForSelectedRow {
                //edit the pair by overwriting it
                if isInSearchMode() {
                    filteredWordPairs[selectedIndexPath.row] = pair
                } else {
                    wordPairs[selectedIndexPath.row] = pair
                }
                
                tableView.reloadRowsAtIndexPaths([selectedIndexPath], withRowAnimation: .None)
            } else {
                //add the new word pair to the word pair array
                if isInSearchMode() {
                    filteredWordPairs.append(pair)
                } else {
                    wordPairs.append(pair)
                }
                //update the tableView
                let indexPath = NSIndexPath(forRow: wordPairs.count-1, inSection: 0)
                tableView.insertRowsAtIndexPaths([indexPath], withRowAnimation: .Automatic)
            }

        }
    }

    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        if tableView.editing {
            self.performSegueWithIdentifier("ViewWordPairDetail", sender: indexPath);
        }
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if segue.identifier == "ViewWordPairDetail" && editing {
            // handle the case where we're editing a word pair
            let navController = segue.destinationViewController as! UINavigationController
            let addWordPairDetail = navController.viewControllers.first as! AddWordPairViewController
            if let selectedCell = sender as? UITableViewCell {
                let indexPath = tableView.indexPathForCell(selectedCell)!
                let selectedPair = getWordPairFromContext(indexPath)
                addWordPairDetail.pair = selectedPair
            }
        }
    }
    
    
    func filterContentForSearchText(searchText: String, searchScope scope: String = "All") {
        filteredWordPairs = wordPairs.filter { pair in
            switch scope {
            case "Phrases":
                return filterWordPair(pair, searchText: searchText)
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
    
    func filterWordPair(pair: WordPhrasePair, searchText: String) -> Bool {
        var nativeMatch = false
        var foreignMatch = false

        if let native = pair.native {
            nativeMatch = native.lowercaseString.containsString(searchText.lowercaseString)
        }
        
        if let foreign = pair.foreign {
            foreignMatch = foreign.lowercaseString.containsString(searchText.lowercaseString)
        }
        
        return (nativeMatch || foreignMatch)
        
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
            removeWordPairFromContext(indexPath)
            try managedObjectContext.save()
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
    
    /** Helper method for getting a word pair 
     This should return the correct word pair regardless of whether a search
     filter is active
     */
    func getWordPairFromContext(indexPath: NSIndexPath) -> WordPhrasePair {
        let pair: WordPhrasePair
        
        if isInSearchMode() {
            pair = filteredWordPairs[indexPath.row]
        } else {
            pair = wordPairs[indexPath.row]
        }
        
        return pair
    }
    
    /** Helper method for removing a word pair
     This should remove the correct word pair regardless of whether a search
     filter is active
     */
    func removeWordPairFromContext(indexPath: NSIndexPath) {
        if isInSearchMode() {
            managedObjectContext.deleteObject(filteredWordPairs[indexPath.row] as NSManagedObject)
            
            let pair = filteredWordPairs[indexPath.row]
            if let wordPairindex = wordPairs.indexOf(pair) {
                filteredWordPairs.removeAtIndex(indexPath.row)
                wordPairs.removeAtIndex(wordPairindex)
            }
        } else {
            managedObjectContext.deleteObject(wordPairs[indexPath.row] as NSManagedObject)
            wordPairs.removeAtIndex(indexPath.row)
        }
    }
    
    /** Check whether the interface is currently in search mode */
    func isInSearchMode() -> Bool {
        return searchController.active && searchController.searchBar.text != ""
    }
}
