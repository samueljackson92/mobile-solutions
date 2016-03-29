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
    
    var pair = WordPhrasePair?()
    var selectedTags = [Tag]()
    
    @IBOutlet weak var nativeWord: UITextField!
    @IBOutlet weak var foreignWord: UITextField!
    @IBOutlet weak var note: UITextView!
    @IBOutlet weak var phraseType: UIPickerView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
    
        // if editing fill the fields
        if let pair = pair {
            nativeWord.text = pair.native
            foreignWord.text   = pair.foreign
            note.text = pair.note
//            phraseType.selectRow(PhraseType.getValueAtIndex(PhraseType(rawValue: pair.type!)), inComponent: 0, animated: true)
        }
    
        phraseType.dataSource = self
        phraseType.delegate = self
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if segue.identifier == "SaveWordPairDetail" {
            saveWordPair()
        } else if segue.identifier == "ViewChooseTags" {
            let tagSelectionController = segue.destinationViewController as! TagSelectionController
            tagSelectionController.delegate = self
            tagSelectionController.selectedTags = pair?.tags!.allObjects as? [Tag]
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
        
        do {
            try managedObjectContext.save()
        } catch {
            fatalError("Failed to save new word phrase: \(error)")
        }
    }
}

extension AddWordPairViewController: TagSelectionDelegate {
    func selectedTags(tags: [Tag]) {
        self.selectedTags = tags
    }
}
