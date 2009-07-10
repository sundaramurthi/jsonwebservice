,"invoke":function(oOperationMetaData,oInput,callback){
	console.log(oOperationMetaData);
	new Ajax.Request(oOperationMetaData.config.endPoint,{
		method:'post',
		requestHeaders: {Accept: oOperationMetaData.config.responseContentType},
		postBody: '{"'+oOperationMetaData.config.requestPayload+'":'+Object.toJSON(oInput)+'}',
		onComplete: callback
	});
},"validateComplexType":function(oOperationMetaData,oInput){
	var errors = new Array();
	for(prop in oOperationMetaData){
		
	}
	return errors;
},"validateSimpleType":function(oDataTypeMetaData,oData){
	console.log(oOperationMetaData);
	console.log(oInput);
	var errors = new Array();
	// Assert oDataTypeMetaData not null
	// Test 1 is nillable?
	if(oData == null && !oDataTypeMetaData.nillable){ // For field null
		var er = {};
		er.type = "nillable";
		er.found = oData;
		er.expected = oDataTypeMetaData.type;
		errors.push(er);
	}
	// Test 2 check required field Should not be empty
	if((errors.length > 0 || oData == "") && oDataTypeMetaData.required){
		var er = {};
		er.type = "required";
		er.found = oData;
		er.expected = oDataTypeMetaData.type;
		errors.push(er);
	}
	// Test 3 check type  
	//if(errors.length > 0 ){
		// If any one above 2 fail, type error  
	//}
	
	// Test 4 restrictions
	
	return errors;
}