//
//  WordPhrasePair+CoreDataProperties.swift
//  WordPairs
//
//  Created by Samuel Jackson on 28/03/2016.
//  Copyright © 2016 Samuel Jackson. All rights reserved.
//
//  Choose "Create NSManagedObject Subclass…" from the Core Data editor menu
//  to delete and recreate this implementation file for your updated model.
//

import Foundation
import CoreData

extension WordPhrasePair {

    @NSManaged var native: String?
    @NSManaged var foreign: String?
    @NSManaged var note: String?
    @NSManaged var tags: NSManagedObject?

}
