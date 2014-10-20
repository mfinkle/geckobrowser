/*
 * Sample script injected into the 'chrome' scope of the GeckoView.
 * This script sets up a simple system of injecting a JS object into all content loaded
 * into the GeckoView.
 */

const { classes: Cc, interfaces: Ci, manager: Cm, utils: Cu, results: Cr } = Components;

Cu.import("resource://gre/modules/Services.jsm");

var BrowserApp = null;

function load(params) {
  console.log("Injected script loaded");

  // Get access to the core BrowserApp object since it has a lot of helpful methods  
  BrowserApp = params.window.BrowserApp;

  // Listen for new content windows to be created
  BrowserApp.deck.addEventListener("DOMWindowCreated", onWindowCreated, true);

  // Listen for new pages to be loaded
  BrowserApp.deck.addEventListener("load", onPageLoad, true);
}

function onWindowCreated(event) {
  // the target is an HTMLDocument
  let contentDocument = event.target;

  // We need the unprotected version of the contentWindow for injecting JS objects
  let unsafeWindow = contentDocument.defaultView.wrappedJSObject;

  let contentObject = {
    message: "Hello from an injected script",
    fetch: function(callback) {
      // Try calling into the host application to get some data and return it to content
      return GeckoView.sendRequestForResult({ type: "fetch" }).then(result => {
        // We need to Cu.cloneInto the 'chrome' result back into the 'content' callback
        callback(Cu.cloneInto(result, unsafeWindow));
      });
    }
  };

  // Use Cu.cloneInto to add the contentObject into the content window
  // https://developer.mozilla.org/en-US/docs/Components.utils.cloneInto
  unsafeWindow.special = Cu.cloneInto(contentObject, unsafeWindow, { cloneFunctions: true });
}

function onPageLoad(event) {
  // the target is an HTMLDocument
  let contentDocument = event.target;

  // We can get the <browser> element used to host the content
  let browser = BrowserApp.getBrowserForDocument(contentDocument);
  console.log("Page loaded: " + browser.contentTitle);
}