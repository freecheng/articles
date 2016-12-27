package fc.compiler.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class CSVParser {
	private CSVLexer lexer;
	
	public CSVParser(Reader reader) {
		this.lexer = new CSVLexer(reader);
	}
	
	public String[] getNextRecord() {
		List<String> record = new ArrayList<String>();
		CSVToken token;
		do {
			token = lexer.getNextToken();
			if (token == CSVToken.FIELD) {
				record.add(lexer.getLexeme());
			}
		} while (token == CSVToken.FIELD || token == CSVToken.COMMENT);
		
		for (String s : record) {
			System.out.println("'" + s + "'");
		}
		
		return record.toArray(new String[0]);
	}
}

enum CSVToken {
	EOF,
	ERROR,
	FIELD,
	END_OF_RECORD,
	COMMENT,
}

class CSVLexer {
    private final char separator;
    private final char quoteChar;
    private final char escape;
    private final char commentStart;
    private final boolean ignoreSurroundingSpaces;
    private final boolean ignoreEmptyLines;

	private char c;		// the current character to be processed */
	private boolean isRealEndOfFile = false;		// whether is at end of input character sequence. 
	private final char END_OF_FILE = 0x1A;	// char literal indicating end of file
	private Reader reader;
	
	private StringBuilder lexemeBuffer = new StringBuilder();
	
	private int fieldTokenCount = 0;		// the number of returned count of fields at current record.
    
    public CSVLexer(Reader reader, char separator, char quoteChar, char escape, 
    		char commentStart, boolean ignoreSurroundingSpaces, boolean ignoreEmptyLines) {
    	this.reader = reader;
    	this.separator = separator;
    	this.quoteChar = quoteChar;
    	this.escape = escape;
    	this.commentStart = commentStart;
    	this.ignoreSurroundingSpaces = ignoreSurroundingSpaces;
    	this.ignoreEmptyLines = ignoreEmptyLines;
    	
    	getNextChar();
    }
    
    public CSVLexer(Reader reader) {
    	this(reader, ',', '"', '\\', '#', true, true);
    }
    
    public CSVToken getNextToken() {
		lexemeBuffer.setLength(0);
    	ignoreSurroundingSpaces();
    	
    	if (c == separator) {
    		fieldTokenCount++;		
    		return CSVToken.FIELD;	// empty field value
    	} else if (c == quoteChar) {
    		return scanQuotedField();
    	} else if (c == '\r' || c == '\n') {
    		if ('\r' == c) {
    			getNextChar();
    		}
    		if ('\n' == c) {
    			getNextChar();
    		}
    		return CSVToken.END_OF_RECORD;
    	} else if (c == END_OF_FILE) {
    		return CSVToken.EOF;
    	} else if (c == commentStart && fieldTokenCount < 1) {
    		return scanComment();
    	} else {
    		return scanSimpleField();
    	}
    }

	private CSVToken scanSimpleField() {
		do {
			lexemeBuffer.append(c);
			getNextChar();
			
			if (c == separator) {
				getNextChar();	// skip next separator char.
				break; 
			} else if (c == '\r' || c == '\n' || c == END_OF_FILE) {
				break;
			}
		} while (true);
		
		ignoreTrailingSpaces(lexemeBuffer);
		
		fieldTokenCount++;		
		return CSVToken.FIELD;
	}

	private CSVToken scanQuotedField() {
		getNextChar();	// skip the starting quote char.
		boolean foundEndingQuoteChar = false;
		while (!foundEndingQuoteChar) {
			if (c == END_OF_FILE) {
				break;
			} else if (c == quoteChar) {
				getNextChar();	// get next char to check
				if (c == quoteChar) {	// 2 continuous quote chars
					lexemeBuffer.append(c);		// add quote char 
					getNextChar();
				} else {
					foundEndingQuoteChar = true;
					if (c == separator) {
						getNextChar();
						break;
					} else {
						ignoreSurroundingSpaces();
						if (c == '\r' || c == '\n' || c == END_OF_FILE) {
							break;
						} else {
							ignoreSurroundingSpaces();
							System.err.println("incorrect char after ending quote char");
							break;
						}
					}
				}
			} else {
				lexemeBuffer.append(c);
				getNextChar();
			}
		};
		
		fieldTokenCount++;		
		return CSVToken.FIELD;
	}
	
	private CSVToken scanComment() {
		do {
			getNextChar();
		} while (c != '\r' && c != '\n');
		return CSVToken.COMMENT;
	}

	public void getNextChar() {
		int i = -1;
		try {
			i = this.reader.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (-1 != i) {
			this.c = (char)i;
		} else { // reach the end of stream or exception occurs
			c = END_OF_FILE;
			isRealEndOfFile = true;
		}
	}
	
	public String getLexeme() {
		return lexemeBuffer.toString();
	}
	
	private void ignoreSurroundingSpaces() {
		if (ignoreSurroundingSpaces) {
    		while (c != '\r' && c != '\n' && c != separator && Character.isWhitespace(c)) {
    			getNextChar();
    		}
    	}
	}
	
    void ignoreTrailingSpaces(final StringBuilder buffer) {
    	if (ignoreSurroundingSpaces) {
	        int length = buffer.length();
	        while (length > 0 && Character.isWhitespace(buffer.charAt(length - 1))) {
	            length = length - 1;
	        }
	        if (length != buffer.length()) {
	            buffer.setLength(length);
	        }
    	}
    }
}
