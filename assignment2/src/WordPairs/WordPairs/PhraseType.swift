//
//  PhraseType.swift
//  WordPairs
//
//  Created by Samuel Jackson on 28/03/2016.
//  Copyright © 2016 Samuel Jackson. All rights reserved.
//

enum PhraseType: String {
    case Noun
    case Verb
    case Adjective
    case NounPhrase
    case VerbPhrase
    
    static func getValueAtIndex(indexOfElement: Int) -> String {
        
        var value = ""
        
        switch indexOfElement {
        case 0:
            value = "Noun"
        case 1:
            value = "Verb"
        case 2:
            value = "Adjective"
        case 3:
            value = "NounPhrase"
        case 4:
            value = "VerbPhrase"
        default:
            value = ""
        }   
        
        return value
    }
    
    static var count: Int { return PhraseType.VerbPhrase.hashValue + 1}
}

