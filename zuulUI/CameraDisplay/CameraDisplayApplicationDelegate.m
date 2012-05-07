//
//  CameraDisplayApplicationDelegate.m
//  CameraDisplay
//
//  Created by Michael Kamphausen on 07.05.12.
//  Copyright (c) 2012 SinnerSchrader Deutschland GmbH. All rights reserved.
//

#import "CameraDisplayApplicationDelegate.h"

@implementation CameraDisplayApplicationDelegate

- (void)applicationDidFinishLaunching:(NSNotification *)aNotification
{
    // open document if none exists
    if ([[[NSDocumentController sharedDocumentController] documents] count] == 0) {
        [[NSDocumentController sharedDocumentController] newDocument:self];
    }
    // send the first window (the opened document) to fullscreen if not in fullscreen yet
    if ([[NSApplication sharedApplication] windows].count > 0) {
        
         NSWindow* window = [[[NSApplication sharedApplication] windows] objectAtIndex:0];
        if ((window.styleMask & NSFullScreenWindowMask) != NSFullScreenWindowMask) {
            [window toggleFullScreen:self];
        }
         
    }
}

@end
