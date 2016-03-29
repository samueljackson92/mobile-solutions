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
    
    var tag = Tag?()
    
    @IBOutlet weak var tagName: UITextField!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        tagName.becomeFirstResponder()

        // if editing fill the fields
        if let tag = tag {
            tagName.text = tag.name
        }
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if segue.identifier == "SaveTagDetail" {
            saveTag()
        }
    }
    
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