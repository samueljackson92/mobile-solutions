//
//  TagWordPairViewController.swift
//  WordPairs
//
//  Created by Samuel Jackson on 29/03/2016.
//  Copyright © 2016 Samuel Jackson. All rights reserved.
//

import UIKit
import CoreData

class TagWordPairViewController: UITableViewController {
    var wordPairs = [WordPhrasePair]()
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return wordPairs.count
    }
    
    // Override to support showing core data objects in the table view.
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier("wordPairCell")
        let pair = wordPairs[indexPath.row]
        prepareCellForDisplay(cell, wordPair: pair)
        return cell!
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