//
//  TestResult.swift
//  WordPairs
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