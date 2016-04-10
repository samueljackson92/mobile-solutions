//
//  AddWordPairViewController.swift
//  WordPairs
//
//  Created by Samuel Jackson on 28/03/2016.
//  Copyright © 2016 Samuel Jackson. All rights reserved.
//

import UIKit
import CoreData

class AddWordPairViewController: UITableViewController, UIPickerViewDataSource, UIPickerViewDelegate {
    
    let managedObjectContext = (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext
    
    // instance variable to store the word pair we're creating
    var pair = WordPhrasePair?()
    
    // list of tags to be associated with the pair.
    // this is set from TagSelectionController.
    var selectedTags = [Tag]()
    
    // register outlets
    @IBOutlet weak var nativeWord: UITextField!
    @IBOutlet weak var foreignWord: UITextField!
    @IBOutlet weak var note: UITextView!
    @IBOutlet weak var phraseType: UIPickerView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
    
        // setup the type spinner
        phraseType.dataSource = self
        phraseType.delegate = self
        
        // if editing fill the fields
        if let pair = pair {
            nativeWord.text = pair.native
            foreignWord.text   = pair.foreign
            note.text = pair.note
            let type = PhraseType.getIndexForValue(pair.type!)
            phraseType.selectRow(type, inComponent: 0, animated: true)
        }
    }
    
    override func shouldPerformSegueWithIdentifier(identifier: String, sender: AnyObject!) -> Bool {
        if identifier == "SaveWordPairDetail" {
            
            // validate text input fields
            if (nativeWord.text!.isEmpty) {
                let message = "Please Enter a Native Word"
                MessageHelper.showValidationMessage(message, controller: self)
                return false
            } else if (foreignWord.text!.isEmpty) {
                let message = "Please Enter a Foreign Word"
                MessageHelper.showValidationMessage(message, controller: self)
                return false
            }
        }
        
        // by default, transition
        return true
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if segue.identifier == "SaveWordPairDetail" {
            // save the segue as we pass back to the previosu screen
            saveWordPair()
        } else if segue.identifier == "ViewChooseTags" {
            // move to the tag selection screen
            let tagSelectionController = segue.destinationViewController as! TagSelectionController
            tagSelectionController.delegate = self
            tagSelectionController.selectedTags = pair?.getAllTagsForWordPair()
        }
    }
    
    func numberOfComponentsInPickerView(pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return PhraseType.count
    }
    
    func pickerView(pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        let type = PhraseType.getValueAtIndex(row)
        return type
    }
    
    /** Create a new word pair instance
     
     This creates a new word pair using the information from the AddWordPairView
     and stores it in core data.
     */
    func saveWordPair() {
        if pair == nil {
            pair = NSEntityDescription.insertNewObjectForEntityForName("WordPhrasePair", inManagedObjectContext: managedObjectContext) as? WordPhrasePair
        }
        pair?.native = nativeWord.text
        pair?.foreign = foreignWord.text
        pair?.note = note.text
        pair?.type = PhraseType.getValueAtIndex(phraseType.selectedRowInComponent(0))
        pair?.addTags(NSSet(array: selectedTags))
        pair?.timeAdded = NSDate()
        
        do {
            try managedObjectContext.save()
        } catch {
            fatalError("Failed to save new word phrase: \(error)")
        }
    }
}

/** Allows the TagSelectionController to pass back the selected tags
*/
extension AddWordPairViewController: TagSelectionDelegate {
    func selectedTags(tags: [Tag]) {
        self.selectedTags = tags
    }
}
