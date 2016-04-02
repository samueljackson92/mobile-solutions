//
//  PhraseType.swift
//  WordPairs
//
//  Created by Samuel Jackson on 28/03/2016.
//  Copyright © 2016 Samuel Jackson. All rights reserved.
//

class PhraseType {
    
    // list of different types of phrases
    // could be expanded to include use persistant storage for dynamically added types
    static var types: [String] = ["", "Noun", "Verb", "Adjective", "Noun Phrase", "Verb Phrase"]
    
    /** Get the value at tht specified index
        
     This is useful when getting the string value from a spinner index
     */
    static func getValueAtIndex(indexOfElement: Int) -> String {
        return types[indexOfElement]
    }
    
    /** Get the index for the given value.
     
     This is useful for setting the selected value on the interface
     */
    static func getIndexForValue(valueOfElement: String) -> Int {
        if let index = types.indexOf(valueOfElement) {
            return index
        }
        // return empty type if not found
        return 0
    }
    
    /** Total number of types */
    static var count: Int { return types.count }
}

