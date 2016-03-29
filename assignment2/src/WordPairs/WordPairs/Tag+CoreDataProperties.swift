//
//  Tag+CoreDataProperties.swift
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

extension Tag {

    @NSManaged var name: String?
    @NSManaged var wordPairs: NSSet?

    @NSManaged func addWordPhrasePairObject(wordPair: WordPhrasePair)
    @NSManaged func removeWordPhrasePairObject(wordPair: WordPhrasePair)
    @NSManaged func addWordPhrasePairs(wordPairs: NSSet)
    @NSManaged func removeWordPhrasePairs(wordPairs: NSSet)
}
