//
//  TestResult.swift
//  WordPairs
// 
//  Class to hold test data passed between interfaces in the testing section
//  of the application. This is neater than passing multiple values.
//
//  Created by Samuel Jackson on 30/03/2016.
//  Copyright © 2016 Samuel Jackson. All rights reserved.
//

import Foundation

class TestData {
    var wordPairs = [WordPhrasePair]()
    var correctCount: Int = 0
    var totalCount: Int = 0
    
    init(wordPairs: [WordPhrasePair]) {
        self.wordPairs = wordPairs
        totalCount = wordPairs.count
    }
}