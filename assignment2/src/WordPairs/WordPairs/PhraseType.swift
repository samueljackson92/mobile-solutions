//
//  PhraseType.swift
//  WordPairs
//
//  Created by Samuel Jackson on 28/03/2016.
//  Copyright © 2016 Samuel Jackson. All rights reserved.
//

class PhraseType {
    
    static var types: [String] = ["", "Noun", "Verb", "Adjective", "Noun Phrase", "Verb Phrase"]
    
    static func getValueAtIndex(indexOfElement: Int) -> String {
        return types[indexOfElement]
    }
    
    static func getIndexForValue(valueOfElement: String) -> Int {
        if let index = types.indexOf(valueOfElement) {
            return index
        }
        // return empty type if not found
        return 0
    }
    
    static var count: Int { return types.count }
}

