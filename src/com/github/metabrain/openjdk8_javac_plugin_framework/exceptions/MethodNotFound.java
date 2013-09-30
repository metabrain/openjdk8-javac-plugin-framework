package com.github.metabrain.openjdk8_javac_plugin_framework.exceptions;

public class MethodNotFound extends RuntimeException {
	public MethodNotFound(String methName) {
		super("Method \""+methName+"\"not found in scope, exiting with error.") ;
	} 
} 
