//
//  TestStartController.swift
//  WordPairs
//
//  Created by Samuel Jackson on 30/03/2016.
//  Copyright © 2016 Samuel Jackson. All rights reserved.
//

import UIKit
import CoreData

class TestStartController: UITableViewController {
    
    let staticTestCells = ["Most Recent"]
    var tags = [Tag]()
 
    override func viewDidLoad() {
        super.viewDidLoad()
        loadTags()
    }
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 2
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        switch (section) {
            case 0:
                return staticTestCells.count
            case 1:
                return tags.count
            default:
                return 0
        }
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier("testCell")
        switch (indexPath.section) {
            case 0:
                cell?.textLabel?.text = staticTestCells[indexPath.row]
                break
            case 1:
                cell?.textLabel?.text = tags[indexPath.row].name
                break
            default:
                 break
        }
        return cell!
    }
    
    override func tableView( tableView : UITableView,  titleForHeaderInSection section: Int)->String {
        switch(section) {
            case 0:
                return ""
            case 1:
                return "Tags"
            default:
                return ""
        }
    }

    
    func loadTags() {
        let managedObjectContext = (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext
        let tagsFetch = NSFetchRequest(entityName: "Tag")
        do {
            tags = try managedObjectContext.executeFetchRequest(tagsFetch) as! [Tag]
        } catch {
            fatalError("Failed to fetch tags: \(error)")
        }
    }
}
