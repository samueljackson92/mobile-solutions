//
//  Tag.swift
//  WordPairs
//
//  Created by Samuel Jackson on 28/03/2016.
//  Copyright © 2016 Samuel Jackson. All rights reserved.
//

import Foundation
import CoreData


class Tag: NSManagedObject {

    func getWordPairs() -> [WordPhrasePair] {
        return self.wordPairs?.allObjects as! [WordPhrasePair]
    }
}
