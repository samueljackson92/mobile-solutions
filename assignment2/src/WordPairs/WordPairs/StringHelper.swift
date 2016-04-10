//
//  StringHelper.swift
//  WordPairs
//
//  Helper extensions to the string class for capilalisting the first letter
//  of a word
//
//  Based on stack overflow question: http://stackoverflow.com/questions/26306326
//  Date: 10/04/16
//

import Foundation

extension String {
    var first: String {
        return String(characters.prefix(1))
    }
    var last: String {
        return String(characters.suffix(1))
    }
    var uppercaseFirst: String {
        return first.uppercaseString + String(characters.dropFirst())
    }
}
