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
    
    let TEST_SIZE = 10
    let staticTestCells = ["Most Recent"]
    var tags = [Tag]()
 
    override func viewDidLoad() {
        super.viewDidLoad()
        loadTags()
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        loadTags()
        tableView.reloadData()
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
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if segue.identifier == "BeginTest" {
            beginTestSegue(segue, sender: sender)
        }
    }
    
    /** Action to handle when a user clicks the cross on the test screen */
    @IBAction func stopTesting(segue:UIStoryboardSegue) {
        // if we cancel we should do nothing
        // just close the segue
    }
    
    func beginTestSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        let navController = segue.destinationViewController as! UINavigationController
        let testController = navController.viewControllers.first as! SimpleTestController
        
        if let selectedCell = sender as? UITableViewCell {
            let indexPath = tableView.indexPathForCell(selectedCell)!
            var testData: TestData?
            
            if indexPath.section == 0 {
                let recentWords = loadMostRecentWordPairs().shuffle()
                testData = TestData(wordPairs: recentWords)
            } else if indexPath.section == 1 {
                let wordPairs = tags[indexPath.row].wordPairs?.allObjects as! [WordPhrasePair]
                testData = TestData(wordPairs: wordPairs.shuffle())
            }
            
            testController.testData = testData!
        }
    }

    func loadMostRecentWordPairs() -> [WordPhrasePair] {
        var mostRecentWords = [WordPhrasePair]()
        let managedObjectContext = (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext
        let wordPairFetch = NSFetchRequest(entityName: "WordPhrasePair")
        do {
            let allWords = try managedObjectContext.executeFetchRequest(wordPairFetch) as! [WordPhrasePair]
            mostRecentWords = allWords.sort({ $0.timeAdded!.compare($1.timeAdded!) == NSComparisonResult.OrderedDescending })
        } catch {
            fatalError("Failed to fetch tags: \(error)")
        }
        return Array(mostRecentWords.prefix(TEST_SIZE))
    }
    
    func loadTags() {
        let managedObjectContext = (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext
        let tagsFetch = NSFetchRequest(entityName: "Tag")
        do {
            let allTags = try managedObjectContext.executeFetchRequest(tagsFetch) as! [Tag]
            tags = allTags.filter { tag in
                return tag.wordPairs!.count > 0
            }
        } catch {
            fatalError("Failed to fetch tags: \(error)")
        }
    }
}
