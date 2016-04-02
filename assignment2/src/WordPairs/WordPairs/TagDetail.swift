//
//  TagDetail.swift
//  WordPairs
//
//  Created by Samuel Jackson on 29/03/2016.
//  Copyright © 2016 Samuel Jackson. All rights reserved.
//

import UIKit
import CoreData

class TagDetail: UIViewController {
    let managedObjectContext = (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext
    
    // instance variable to store new tag
    var tag = Tag?()
    
    // register outlets
    @IBOutlet weak var tagName: UITextField!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        tagName.becomeFirstResponder()

        // if editing fill the fields
        if let tag = tag {
            tagName.text = tag.name
        }
    }
    
    override func shouldPerformSegueWithIdentifier(identifier: String, sender: AnyObject!) -> Bool {
        if identifier == "SaveTagDetail" {
            
            // Only segue if the user has entered something
            if tagName.text!.isEmpty {
                let message = "Please Enter a Name for the Tag"
                MessageHelper.showValidationMessage(message, controller: self)
                return false
            }
        }
        
        return true
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if segue.identifier == "SaveTagDetail" {
            saveTag()
        }
    }
    
    
    /** Create a new tag instance
     
     This creates a new tag and stores it in core data.
     */
    func saveTag() {
        if tag == nil {
            tag = NSEntityDescription.insertNewObjectForEntityForName("Tag", inManagedObjectContext: managedObjectContext) as? Tag
        }
        
        tag?.name = tagName.text
        
        do {
            try managedObjectContext.save()
        } catch {
            fatalError("Failed to save new tag: \(error)")
        }
    }
    
}