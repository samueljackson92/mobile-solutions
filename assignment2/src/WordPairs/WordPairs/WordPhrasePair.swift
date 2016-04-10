//
//  WordPhrasePair.swift
//  WordPairs
//
//  Created by Samuel Jackson on 28/03/2016.
//  Copyright © 2016 Samuel Jackson. All rights reserved.
//

import Foundation
import CoreData


class WordPhrasePair: NSManagedObject {
    
    func getAllTagsForWordPair() -> [Tag] {
        return self.tags!.allObjects as! [Tag]
    }

}
