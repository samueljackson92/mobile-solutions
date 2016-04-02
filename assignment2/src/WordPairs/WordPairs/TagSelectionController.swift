//
//  TagSelectionController.swift
//  WordPairs
//
//  Created by Samuel Jackson on 29/03/2016.
//  Copyright © 2016 Samuel Jackson. All rights reserved.
//

import UIKit
import CoreData

protocol TagSelectionDelegate {
    func selectedTags(tags: [Tag])
}

class TagSelectionController: UITableViewController {
    
    let managedObjectContext = (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext
    var delegate: TagSelectionDelegate?
    
    // flag stating if the interface is interactive or not
    // this let us reuse the view depending on whether we're editing or not
    var interactive = true
    
    // instance variables to store all the tags and the list of
    // selected tags
    var tags = [Tag]()
    var selectedTags = [Tag]?()
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // only load all tags when we're in interactive mode
        // otherwise we only want to show the tags associated with
        // a particular word
        if interactive {
            loadTags()
        }
        
        if let selectedTags = selectedTags {
            let selectedIndicies = selectedTags.map { tag in
                tags.indexOf(tag)
            }
            selectedIndicies.forEach { index in
                let indexPath = NSIndexPath(forRow: index!, inSection: 0)
                tableView.selectRowAtIndexPath(indexPath, animated: false, scrollPosition: UITableViewScrollPosition.None)
                tableView.cellForRowAtIndexPath(indexPath)?.accessoryType = UITableViewCellAccessoryType.Checkmark
            }
        }
    }
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return tags.count
    }
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.cellForRowAtIndexPath(indexPath)?.accessoryType = UITableViewCellAccessoryType.Checkmark
    }
    
    override func tableView(tableView: UITableView, didDeselectRowAtIndexPath indexPath: NSIndexPath) {
        tableView.cellForRowAtIndexPath(indexPath)?.accessoryType = UITableViewCellAccessoryType.None
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier("tagCell")
        let tag = tags[indexPath.row]
        cell?.textLabel!.text = tag.name
        
        // if this isn't interactive then prevent user interaction
        if !interactive {
            cell?.selectionStyle = UITableViewCellSelectionStyle.None
            cell?.userInteractionEnabled = false
            cell?.accessoryType = UITableViewCellAccessoryType.None
        }
        
        return cell!
    }
    
    override func willMoveToParentViewController(parent: UIViewController?) {
        super.willMoveToParentViewController(parent)
        if parent == nil {
            if let indicies = tableView.indexPathsForSelectedRows {
                var selectedTags = [Tag]()
                
                // collect all of the selected tag objects
                indicies.forEach { index in
                    selectedTags.append(tags[index.row])
                }
                
                // pass the tags we chose back to the add word pair view
                self.delegate?.selectedTags(selectedTags)
            }
        }
    }
    
    /** Load all of the tags from the Core Data */
    func loadTags() {
        let tagsFetch = NSFetchRequest(entityName: "Tag")
        do {
            tags = try managedObjectContext.executeFetchRequest(tagsFetch) as! [Tag]
        } catch {
            fatalError("Failed to fetch tags: \(error)")
        }
    }
}
