package com.github.metabrain.openjdk8_javac_plugin_framework.exceptions;

public class EnclosingMethodNotFound extends RuntimeException {
	public EnclosingMethodNotFound() {
		super("Enclosing method not found, exiting with error") ;
	}
}
