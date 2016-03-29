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
        self.tableView.tableHeaderView = searchController.searchBar

        loadTags()
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
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
        
        if searchController.active && searchController.searchBar.text != "" {
            tag = filteredTags[indexPath.row]
        } else {
            tag = tags[indexPath.row]
        }
        
        let wordPairCount = tag.wordPairs!.count
        cell?.textLabel!.text = tag.name
        cell?.detailTextLabel!.text = "Phrases \(wordPairCount)"
        return cell!
    }
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        if tableView.editing {
            self.performSegueWithIdentifier("ViewTagDetail", sender: indexPath);
        }
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if segue.identifier == "ViewTagDetail" && editing {
            // handle the case where we're editing a word pair
            let navController = segue.destinationViewController as! UINavigationController
            let tagDetail = navController.viewControllers.first as! TagDetail
            if let selectedCell = sender as? UITableViewCell {
                let indexPath = tableView.indexPathForCell(selectedCell)!
                let tag = tags[indexPath.row]
                tagDetail.tag = tag
            }
        } else if segue.identifier == "ViewWordsForTag" {
            let tagWordPairController = segue.destinationViewController as! TagWordPairViewController
            if let selectedCell = sender as? UITableViewCell {
                let indexPath = tableView.indexPathForCell(selectedCell)!
                let tag = tags[indexPath.row]
                let name = tag.name!
                tagWordPairController.title = "Word Pairs for \(name)"
                tagWordPairController.wordPairs = tag.wordPairs!.allObjects as! [WordPhrasePair]
            }
        }
    }
    
    func updateSearchResultsForSearchController(searchController: UISearchController){
        filterContentForSearchText(searchController.searchBar.text!)
    }
    
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
                    tags[selectedIndexPath.row] = tag
                    tableView.reloadRowsAtIndexPaths([selectedIndexPath], withRowAnimation: .None)
                } else {
                    //add the new tag to the tag array
                    tags.append(tag)
                    
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
            managedObjectContext.deleteObject(tags[indexPath.row] as NSManagedObject)
            try managedObjectContext.save()
            tags.removeAtIndex(indexPath.row)
            tableView.deleteRowsAtIndexPaths([indexPath], withRowAnimation: .Fade)
        } catch {
            fatalError("Failed to delete object: \(error)")
        }
    }
    
    func loadTags() {
        let tagsFetch = NSFetchRequest(entityName: "Tag")
        do {
            tags = try managedObjectContext.executeFetchRequest(tagsFetch) as! [Tag]
        } catch {
            fatalError("Failed to fetch tags: \(error)")
        }
    }
}
