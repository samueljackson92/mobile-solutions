//
//  StringHelper.swift
//  WordPairs
//
//  Created by Samuel Jackson on 02/04/2016.
//  Copyright © 2016 Samuel Jackson. All rights reserved.
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
