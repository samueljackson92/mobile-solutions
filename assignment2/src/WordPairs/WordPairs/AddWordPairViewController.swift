//
//  AddWordPairViewController.swift
//  WordPairs
//
//  Created by Samuel Jackson on 28/03/2016.
//  Copyright © 2016 Samuel Jackson. All rights reserved.
//

import UIKit
import CoreData

class AddWordPairViewController: UITableViewController {
    
    let managedObjectContext = (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext
    var pair = WordPhrasePair?()
    
    @IBOutlet weak var nativeWord: UITextField!
    @IBOutlet weak var foreignWord: UITextField!
    @IBOutlet weak var note: UITextView!
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if segue.identifier == "SaveWordPairDetail" {
            saveWordPair()
        }
    }
    
    /** Create a new word pair instance
     
     This creates a new word pair using the information from the AddWordPairView
     and stores it in core data.
     */
    func saveWordPair() {
        pair = NSEntityDescription.insertNewObjectForEntityForName("WordPhrasePair", inManagedObjectContext: managedObjectContext) as? WordPhrasePair
        pair?.native = nativeWord.text
        pair?.foreign = foreignWord.text
        pair?.note = note.text
        
        do {
            try managedObjectContext.save()
        } catch {
            fatalError("Failed to save new word phrase: \(error)")
        }
    }
}
