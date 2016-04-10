//
//  WordPairDetailStatic.swift
//  WordPairs
//
//  Created by Samuel Jackson on 29/03/2016.
//  Copyright © 2016 Samuel Jackson. All rights reserved.
//

import UIKit
import CoreData

class WordPairStaticDetailController: UITableViewController {
    var pair: WordPhrasePair?
    
    @IBOutlet weak var nativeWord: UITableViewCell!
    @IBOutlet weak var foreignWord: UITableViewCell!
    @IBOutlet weak var type: UITableViewCell!
    @IBOutlet weak var note: UITextView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if let pair = pair {
            nativeWord.textLabel?.text = pair.native
            foreignWord.textLabel?.text = pair.foreign
            note.text = pair.note
            type.textLabel?.text = pair.type
            
            let tags = pair.getTags()
            
            // insert all of the tags onto the page
            for (var row = 0; row < tags.count; row++) {
                let index = NSIndexPath(forRow: row, inSection: 2)
                tableView.insertRowsAtIndexPaths([index], withRowAnimation: .None)
                let cell = tableView.cellForRowAtIndexPath(index)
                cell?.textLabel?.text = tags[row].name
            }
        }
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if segue.identifier == "ViewTags" {
            // segue back to view the tags associated with word pair
            let tagSelectionController = segue.destinationViewController as! TagSelectionController
            tagSelectionController.tags = (pair?.getTags())!
            tagSelectionController.interactive = false
        }
    }
}
