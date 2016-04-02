//
//  MessageHelper.swift
//  WordPairs
//
//  Created by Samuel Jackson on 02/04/2016.
//  Copyright © 2016 Samuel Jackson. All rights reserved.
//

import UIKit

class MessageHelper {
    static let messageTitle = "Word Pairs"
    
    static func showValidationMessage(message: String, controller: UIViewController) {
        let alertController = UIAlertController(title: messageTitle, message: message, preferredStyle: .Alert)
        alertController.addAction(UIAlertAction(title: "OK", style: .Default, handler: nil))
        controller.presentViewController(alertController, animated: true, completion: nil)
    }
    
    static func alertSuccess(message: String, controller: UIViewController, handler: ((UIAlertAction) -> Void)?) {
        let alertController = UIAlertController(title: messageTitle, message:message, preferredStyle: .Alert)
        alertController.addAction(UIAlertAction(title: "Continue", style: UIAlertActionStyle.Default, handler: handler))
        controller.presentViewController(alertController, animated: true, completion: nil)
    }
    
    static func alertFailure(message: String, controller: UIViewController) {
        let alertController = UIAlertController(title: messageTitle, message: message, preferredStyle: .Alert)
        alertController.addAction(UIAlertAction(title: "Dismiss", style: UIAlertActionStyle.Default, handler: nil))
        controller.presentViewController(alertController, animated: true, completion: nil)
    }
}