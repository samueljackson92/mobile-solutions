//
//  SimpleTestController.swift
//  WordPairs
//
//  Created by Samuel Jackson on 30/03/2016.
//  Copyright © 2016 Samuel Jackson. All rights reserved.
//

import UIKit

class SimpleTestController: UIViewController {
    
    var testData: TestData?
    var currentWordPhrase: WordPhrasePair?
    var currentWordIndex: Int = 0
    var wordSubmitted: Bool = false
    
    @IBOutlet weak var foreignWord: UILabel!
    @IBOutlet weak var currentWord: UILabel!
    @IBOutlet weak var currentGuess: UITextField!
    @IBOutlet weak var result: UILabel!
    @IBOutlet weak var submit: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        currentWordIndex = 0
        testData?.correctCount = 0
        setupTestStep()
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if segue.identifier == "ReviewTest" {
            let navController = segue.destinationViewController as! UINavigationController
            let reviewTest = navController.viewControllers.first as! TestReviewController
            reviewTest.testData = testData!
        }
    }
    
    @IBAction func submitButtonClicked(sender: AnyObject) {
        if(!wordSubmitted) {
            if (currentGuess.text!.isEmpty) {
                let message = "Please Enter a Guess"
                MessageHelper.showValidationMessage(message, controller: self)
            } else {
                changeToGuessedState()
            }
        } else {
            changeToNextWordOrReview()
        }
    }
    
    /* Reset the UI ready for another test */
    func setupTestStep() {
        wordSubmitted = false
        currentWordPhrase = testData?.wordPairs[currentWordIndex]
        result.text = ""
        currentGuess.text = ""
        foreignWord.text = ""
        currentWord.text = currentWordPhrase!.native
        submit.setTitle("OK", forState: .Normal)
    }
    
    /* Change the guessed state after the user has submitted a guess */
    func changeToGuessedState() {
        wordSubmitted = true
        // check guess is correct and update state
        if checkGuessIsCorrect() {
            result.textColor = UIColor.greenColor()
            result.text = "Correct!"
            testData?.correctCount++
        } else {
            result.textColor = UIColor.redColor()
            result.text = "Incorrect!"
        }
        
        foreignWord.text = currentWordPhrase?.foreign
        // set button to continue to next word
        submit.setTitle("Continue", forState: .Normal)
    }
    
    /* Move onto the next word or go to the review view */
    func changeToNextWordOrReview() {
        // word has been guessed, continue to next word
        ++currentWordIndex
        // check if we need to test more words
        if currentWordIndex < testData?.wordPairs.count {
            setupTestStep()
        } else {
            // continue to review
            self.performSegueWithIdentifier("ReviewTest", sender: submit);
        }
    }
    
    /* Helper function to check if the guess made matched the word */
    func checkGuessIsCorrect() -> Bool {
        let guess = currentGuess.text?.lowercaseString
        let actual = currentWordPhrase?.foreign?.lowercaseString
        return guess == actual
    }
}
