//
//  AddWordPairViewController.swift
//  WordPairs
//
//  Created by Samuel Jackson on 28/03/2016.
//  Copyright © 2016 Samuel Jackson. All rights reserved.
//

import UIKit
import CoreData

class AddWordPairViewController: UIViewController {
    
    let managedObjectContext = (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext
    var pair = WordPhrasePair?()
    
    @IBOutlet weak var nativeWord: UITextField!
    @IBOutlet weak var foreignWord: UITextField!
    @IBOutlet weak var note: UITextView!
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if segue.identifier == "SaveWordPairDetail" {
            pair = NSEntityDescription.insertNewObjectForEntityForName("WordPhrasePair", inManagedObjectContext: managedObjectContext) as? WordPhrasePair
            pair?.native = nativeWord.text
            pair?.foreign = foreignWord.text
            pair?.note = note.text

            do {
                try managedObjectContext.save()
            } catch {
                
            }
        }
    }
}
