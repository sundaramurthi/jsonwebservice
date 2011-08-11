(function () {
	var util = YAHOO.util, lang = YAHOO.lang, Event = util.Event, Dom = util.Dom;
	
	inputEx.JSONForm = function(options) {
	   inputEx.JSONForm.superclass.constructor.call(this, options);
	};

	lang.extend(inputEx.JSONForm, inputEx.Form, {
		 /**
		    * Send the form value in JSON through an ajax request
		    */
		   asyncRequest: function() {
		      if(this.options.ajax.showMask) { this.showMask(); }
			
				var formValue = this.inputs[0].getValue();
			
				// options.ajax.uri and options.ajax.method can also be functions that return a the uri/method depending of the value of the form
				var uri = lang.isFunction(this.options.ajax.uri) ? this.options.ajax.uri(formValue) : this.options.ajax.uri;
				var method = lang.isFunction(this.options.ajax.method) ? this.options.ajax.method(formValue) : this.options.ajax.method;
			
				var postData = null;
				
				// Classic application/x-www-form-urlencoded (like html forms)
				if(this.options.ajax.contentType == "application/x-www-form-urlencoded" && method != "PUT") {
					var params = [];
					for(var key in formValue) {
						if(formValue.hasOwnProperty(key)) {
							var pName = (this.options.ajax.wrapObject ? this.options.ajax.wrapObject+'[' : '')+key+(this.options.ajax.wrapObject ? ']' : '');
							params.push( pName+"="+window.encodeURIComponent(formValue[key]));
						}
					}
					postData = params.join('&');
				}
				// The only other contentType available is "application/json"
				else {
					util.Connect.setDefaultPostHeader(false);
					util.Connect.initHeader("Content-Type" , "application/json" , false);
					var p;
					if(this.options.ajax.wrapObject) {
						p = {};
						p[this.options.ajax.wrapObject] = formValue;
					}
					else {
						p = formValue;
					}
					postData = lang.JSON.stringify(p);
				}
				
		      util.Connect.asyncRequest( method, uri, {
		         success: function(o) {
		            if(this.options.ajax.showMask) { this.hideMask(); }
		            if( lang.isFunction(this.options.ajax.callback.success) ) {
		               this.options.ajax.callback.success.call(this.options.ajax.callback.scope,o);
		            }
		         },

		         failure: function(o) {
		            if(this.options.ajax.showMask) { this.hideMask(); }
		            if( lang.isFunction(this.options.ajax.callback.failure) ) {
		               this.options.ajax.callback.failure.call(this.options.ajax.callback.scope,o);
		            }
		         },

		         scope:this
		      }, postData);
		   }
	});
})();
