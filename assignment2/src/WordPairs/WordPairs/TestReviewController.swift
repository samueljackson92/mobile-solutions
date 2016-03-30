//
//  TestReviewController.swift
//  WordPairs
//
//  Created by Samuel Jackson on 30/03/2016.
//  Copyright © 2016 Samuel Jackson. All rights reserved.
//

import UIKit

class TestReviewController: UIViewController {

    var testData: TestData?
    
    @IBOutlet weak var score: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        score.text = "\(testData!.correctCount) / \(testData!.totalCount)"
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if segue.identifier == "RetryTest" {
            let navController = segue.destinationViewController as! UINavigationController
            let testController = navController.viewControllers.first as! SimpleTestController
            testController.testData = TestData(wordPairs: (testData?.wordPairs.shuffle())!)
        }
    }
}
