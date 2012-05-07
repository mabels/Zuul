/*
     File: AVRecorderDocument.m
 Abstract: AVRecorder document containing all of the logic for communicating information from the UI to the capture session.
  Version: 1.0
 
 Disclaimer: IMPORTANT:  This Apple software is supplied to you by Apple
 Inc. ("Apple") in consideration of your agreement to the following
 terms, and your use, installation, modification or redistribution of
 this Apple software constitutes acceptance of these terms.  If you do
 not agree with these terms, please do not use, install, modify or
 redistribute this Apple software.
 
 In consideration of your agreement to abide by the following terms, and
 subject to these terms, Apple grants you a personal, non-exclusive
 license, under Apple's copyrights in this original Apple software (the
 "Apple Software"), to use, reproduce, modify and redistribute the Apple
 Software, with or without modifications, in source and/or binary forms;
 provided that if you redistribute the Apple Software in its entirety and
 without modifications, you must retain this notice and the following
 text and disclaimers in all such redistributions of the Apple Software.
 Neither the name, trademarks, service marks or logos of Apple Inc. may
 be used to endorse or promote products derived from the Apple Software
 without specific prior written permission from Apple.  Except as
 expressly stated in this notice, no other rights or licenses, express or
 implied, are granted by Apple herein, including but not limited to any
 patent rights that may be infringed by your derivative works or by other
 works in which the Apple Software may be incorporated.
 
 The Apple Software is provided by Apple on an "AS IS" basis.  APPLE
 MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS
 FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND
 OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS.
 
 IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL
 OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION,
 MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED
 AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE),
 STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 
 Copyright (C) 2011 Apple Inc. All Rights Reserved.
 
 */

#import "CameraDisplayDocument.h"
#include <stdlib.h>

@interface CameraDisplayDocument ()

// Properties for internal use
@property (retain) AVCaptureDeviceInput *videoDeviceInput;
@property (retain) AVCaptureVideoPreviewLayer *previewLayer;
@property (retain) NSArray *observers;

// Methods for internal use
- (void)refreshDevices;
- (void)updateWebView:(NSDictionary *)params;

@end

int const PROCESS_NTH_FRAME = 10;
NSString* const CameraDisplayDocumentUpdateWebView = @"CameraDisplayDocumentUpdateWebView";

@implementation CameraDisplayDocument

@synthesize videoDeviceInput;
@synthesize videoDevices;
@synthesize session;
@synthesize previewView;
@synthesize webView;
@synthesize previewLayer;
@synthesize observers;

- (id)init
{
	self = [super init];
	if (self) {
        
        baseUrl = @"http://wifi.nextconf.eu/WiFi/Pass/app";
		// Create a capture session
		session = [[AVCaptureSession alloc] init];
        
        // Add video data output
        AVCaptureVideoDataOutput *videoDataOutput = [[AVCaptureVideoDataOutput alloc] init];
        videoDataOutput.videoSettings = [NSDictionary dictionaryWithObject:[NSNumber numberWithInt:kCVPixelFormatType_422YpCbCr8] forKey:(id)kCVPixelBufferPixelFormatTypeKey];
        dispatch_queue_t my_queue = dispatch_queue_create("com.sinnerschrader.CameraDisplay.captureVideoFrames", NULL);
        [videoDataOutput setSampleBufferDelegate:self queue:my_queue];
        dispatch_release(my_queue);
        if ([session canAddOutput:videoDataOutput]) {
            [session addOutput:videoDataOutput];
        }
        else {
            NSLog(@"Error: could not add video output");
            // Handle the failure.
        }
        //[self logAvailableTypes:videoDataOutput];
        NSLog(@"videoDataOutput.videoSettings %@", videoDataOutput.videoSettings);
        [videoDataOutput release];
        
		
		// Capture Notification Observers
		NSNotificationCenter *notificationCenter = [NSNotificationCenter defaultCenter];
		id runtimeErrorObserver = [notificationCenter addObserverForName:AVCaptureSessionRuntimeErrorNotification
																  object:session
																   queue:[NSOperationQueue mainQueue]
															  usingBlock:^(NSNotification *note) {
																  dispatch_async(dispatch_get_main_queue(), ^(void) {
																	  [self presentError:[[note userInfo] objectForKey:AVCaptureSessionErrorKey]];
																  });
															  }];
		id didStartRunningObserver = [notificationCenter addObserverForName:AVCaptureSessionDidStartRunningNotification
																	 object:session
																	  queue:[NSOperationQueue mainQueue]
																 usingBlock:^(NSNotification *note) {
																	 NSLog(@"did start running");
																 }];
		id didStopRunningObserver = [notificationCenter addObserverForName:AVCaptureSessionDidStopRunningNotification
																	object:session
																	 queue:[NSOperationQueue mainQueue]
																usingBlock:^(NSNotification *note) {
																	NSLog(@"did stop running");
																}];
		id deviceWasConnectedObserver = [notificationCenter addObserverForName:AVCaptureDeviceWasConnectedNotification
																		object:nil
																		 queue:[NSOperationQueue mainQueue]
																	usingBlock:^(NSNotification *note) {
																		[self refreshDevices];
																	}];
		id deviceWasDisconnectedObserver = [notificationCenter addObserverForName:AVCaptureDeviceWasDisconnectedNotification
																		   object:nil
																			queue:[NSOperationQueue mainQueue]
																	   usingBlock:^(NSNotification *note) {
																		   [self refreshDevices];
																	   }];
		id updateWebViewObserver = [notificationCenter addObserverForName:CameraDisplayDocumentUpdateWebView
																		   object:nil
																			queue:[NSOperationQueue mainQueue]
																	   usingBlock:^(NSNotification *note) {
																		   [self updateWebView:note.userInfo];
																	   }];
		observers = [[NSArray alloc] initWithObjects:runtimeErrorObserver, didStartRunningObserver, didStopRunningObserver, deviceWasConnectedObserver, deviceWasDisconnectedObserver, updateWebViewObserver, nil];
		
		// Select devices if any exist
		AVCaptureDevice *videoDevice = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
		if (videoDevice) {
			[self setSelectedVideoDevice:videoDevice];
		} else {
			[self setSelectedVideoDevice:[AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeMuxed]];
		}
		
		// Initial refresh of device list
		[self refreshDevices];
        
        frameCount = 0;
        scanner = new ImageScanner();
        scanner->set_config(ZBAR_NONE, ZBAR_CFG_ENABLE, 1);
	}
	return self;
}

- (void)windowWillClose:(NSNotification *)notification
{
	
	// Stop the session
	[[self session] stopRunning];
	
	// Remove Observers
	NSNotificationCenter *notificationCenter = [NSNotificationCenter defaultCenter];
	for (id observer in [self observers])
		[notificationCenter removeObserver:observer];
	[observers release];
}

- (void)dealloc
{
	[videoDevices release];
	[session release];
	[previewLayer release];
	[videoDeviceInput release];
	
	[super dealloc];
}

- (NSString *)windowNibName
{
	return @"CameraDisplayDocument";
}

- (void)windowControllerDidLoadNib:(NSWindowController *) aController
{
	[super windowControllerDidLoadNib:aController];
	
	// Attach preview to session
	CALayer *previewViewLayer = [[self previewView] layer];
	[previewViewLayer setBackgroundColor:CGColorGetConstantColor(kCGColorBlack)];
	AVCaptureVideoPreviewLayer *newPreviewLayer = [[AVCaptureVideoPreviewLayer alloc] initWithSession:[self session]];
	[newPreviewLayer setFrame:[previewViewLayer bounds]];
	[newPreviewLayer setAutoresizingMask:kCALayerWidthSizable | kCALayerHeightSizable];
	[previewViewLayer addSublayer:newPreviewLayer];
	[self setPreviewLayer:newPreviewLayer];
	[newPreviewLayer release];
    
    NSDictionary *userInfo = [NSDictionary dictionaryWithObject:baseUrl forKey:@"URL"];
    [[NSNotificationCenter defaultCenter] postNotificationName:CameraDisplayDocumentUpdateWebView object:self userInfo:userInfo];

	// Start the session
	[[self session] startRunning];
}

- (void)didPresentErrorWithRecovery:(BOOL)didRecover contextInfo:(void  *)contextInfo
{
	// Do nothing
}

- (void) logAvailableTypes:(AVCaptureVideoDataOutput*)videoDataOutput
{
    for (int i = 0, maxI = (int)[videoDataOutput.availableVideoCVPixelFormatTypes count]; i < maxI; i++) {
        int num = [(NSNumber*)[videoDataOutput.availableVideoCVPixelFormatTypes objectAtIndex:i] intValue];
        NSString * str = [NSString stringWithFormat:@"%x", num];
        NSMutableString * newString = [[[NSMutableString alloc] init] autorelease];
        int j = 0;
        while (j < [str length])
        {
            NSString * hexChar = [str substringWithRange: NSMakeRange(j, 2)];
            int value = 0;
            sscanf([hexChar cStringUsingEncoding:NSASCIIStringEncoding], "%x", &value);
            [newString appendFormat:@"%c", (char)value];
            j+=2;
        }
        NSLog(@"%i = 0x%@ = %@", num, str, newString);
    }
}

#pragma mark Device selection
- (void)refreshDevices
{
	[self setVideoDevices:[[AVCaptureDevice devicesWithMediaType:AVMediaTypeVideo] arrayByAddingObjectsFromArray:[AVCaptureDevice devicesWithMediaType:AVMediaTypeMuxed]]];
	
	[[self session] beginConfiguration];
	
	if (![[self videoDevices] containsObject:[self selectedVideoDevice]])
		[self setSelectedVideoDevice:nil];
	
	[[self session] commitConfiguration];
}

- (AVCaptureDevice *)selectedVideoDevice
{
	return [videoDeviceInput device];
}

- (void)setSelectedVideoDevice:(AVCaptureDevice *)selectedVideoDevice
{
	[[self session] beginConfiguration];
	
	if ([self videoDeviceInput]) {
		// Remove the old device input from the session
		[session removeInput:[self videoDeviceInput]];
		[self setVideoDeviceInput:nil];
	}
	
	if (selectedVideoDevice) {
		NSError *error = nil;
		
		// Create a device input for the device and add it to the session
		AVCaptureDeviceInput *newVideoDeviceInput = [AVCaptureDeviceInput deviceInputWithDevice:selectedVideoDevice error:&error];
		if (newVideoDeviceInput == nil) {
			dispatch_async(dispatch_get_main_queue(), ^(void) {
				[self presentError:error];
			});
		} else {
			if (![selectedVideoDevice supportsAVCaptureSessionPreset:[session sessionPreset]])
				[[self session] setSessionPreset:AVCaptureSessionPresetHigh];
			
			[[self session] addInput:newVideoDeviceInput];
			[self setVideoDeviceInput:newVideoDeviceInput];
		}
	}
	
	[[self session] commitConfiguration];
}

#pragma mark Video frame processing
- (void)captureOutput:(AVCaptureOutput *)captureOutput didOutputSampleBuffer:(CMSampleBufferRef)sampleBuffer fromConnection:(AVCaptureConnection *)connection
{
    frameCount++;
    if (frameCount == PROCESS_NTH_FRAME) {
        CVImageBufferRef imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer); 
        CVPixelBufferLockBaseAddress(imageBuffer,0); 
        uint8_t *baseAddress = (uint8_t *)CVPixelBufferGetBaseAddress(imageBuffer); 
        size_t bytesPerRow = CVPixelBufferGetBytesPerRow(imageBuffer); 
        size_t width = CVPixelBufferGetWidth(imageBuffer); 
        size_t height = CVPixelBufferGetHeight(imageBuffer);
        //OSType pixelFormat = CVPixelBufferGetPixelFormatType(imageBuffer);
        
        if (NO) {
            NSLog(@"%s", baseAddress);
        }
        
        //NSLog(@"processing frame %u, %lu, %lu %lu", pixelFormat, bytesPerRow, width, height);
      
        uint8 *grayscaleBuffer = 0;
        size_t grayscaleBufferSize = 0;
        grayscaleBufferSize = (bytesPerRow*height)/2;
        grayscaleBuffer = (uint8*)malloc(grayscaleBufferSize);
         memset(grayscaleBuffer, 0, grayscaleBufferSize);
        uint8 *sourceMemPos = baseAddress + 1;
        uint8 *destinationMemPos = grayscaleBuffer;
        uint8 *destinationEnd = grayscaleBuffer + grayscaleBufferSize;
        while (destinationMemPos < destinationEnd) {
            memcpy(destinationMemPos, sourceMemPos, 1);
            destinationMemPos += 1;
            sourceMemPos += 2;
        }
        
        
        Image image((unsigned)width, (unsigned)height, "Y800", grayscaleBuffer, (unsigned long)(width*height));
        //Image img = image.convert(0x20203859);
        scanner->scan(image);
        for(Image::SymbolIterator symbol = image.symbol_begin();
            symbol != image.symbol_end();
            ++symbol) {
            // do something useful with results
            //NSLog(@"******************************");
            NSString *url = [[NSString stringWithCString:symbol->get_data().c_str() encoding:NSUTF8StringEncoding]stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
            NSLog(@"decoded %@", url);
            /*cout << "decoded " << symbol->get_type_name()
            << " symbol \"" << symbol->get_data() << '"' << endl;
            imwrite("found.jpg", edges);*/
            //NSLog(@"Jo %@", [NSString stringWithFormat:@"%@#%@", baseUrl, url]);
            NSDictionary *userInfo = [NSDictionary dictionaryWithObject:[NSString stringWithCString:symbol->get_data().c_str() encoding:NSUTF8StringEncoding] forKey:@"DATA"];
            [[NSNotificationCenter defaultCenter] postNotificationName:CameraDisplayDocumentUpdateWebView object:self userInfo:userInfo];

        
        }
        free(grayscaleBuffer);
        frameCount = 0;
    }
}

- (void)updateWebView:(NSDictionary *)params
{

    NSLog(@"URL %@", [params valueForKey:@"URL"]);
    NSLog(@"DATA %@", [params valueForKey:@"DATA"]);
    if ([params objectForKey:@"URL"]) {
        NSLog(@"FOUND URL");
        [self.webView.mainFrame loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:[params valueForKey:@"URL"]]]];
    } 
    if ([params objectForKey:@"DATA"]) {
        NSLog(@"FOUND Data");
       [self.webView stringByEvaluatingJavaScriptFromString:[NSString stringWithFormat:@"nativeCallback('%@');", [params valueForKey:@"DATA"]]];
    } 
    
    /*
    [self.webView.mainFrame loadHTMLString:[NSString stringWithFormat:@"<html><head></head><body>Random Number: %@</body></html>", [params valueForKey:@"URL"]] baseURL:nil];
    */
    
}

@end
