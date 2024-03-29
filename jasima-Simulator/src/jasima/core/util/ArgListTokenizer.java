/*******************************************************************************
 * This file is part of jasima, v1.3, the Java simulator for manufacturing and 
 * logistics.
 *  
 * Copyright (c) 2015 		jasima solutions UG
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package jasima.core.util;

import java.util.Arrays;
import java.util.Objects;

/**
 * Splits an input string in tokens, so lists and parameter lists can be
 * recognized. This class recognizes (quoted) Strings with back-slash as an
 * escape character, white space, and as special one-character tokens: "(", ")",
 * ";", "=".
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 */
public class ArgListTokenizer {

	/**
	 * The recognized tokens.
	 */
	public static enum TokenType {
		STRING, PARENS_OPEN, PARENS_CLOSE, EQUALS, SEMICOLON, WHITE_SPACE
	};

	/**
	 * A {@code ParseException} is thrown if there were problems splitting the
	 * input string into tokens.
	 */
	public static class ParseException extends RuntimeException {

		private static final long serialVersionUID = 3473197915435659395L;

		private String msg;
		private Object[] msgParams;

		public ParseException(int errorPos, String msg, Object... msgParams) {
			super();
			this.msg = msg;
			this.msgParams = new Object[1 + msgParams.length];
			this.msgParams[0] = errorPos;
			for (int i = 0; i < msgParams.length; i++) {
				this.msgParams[i + 1] = msgParams[i];
			}
		}

		@Override
		public String getMessage() {
			return String.format(Util.DEF_LOCALE,
					"Parse error at or before position %d: " + msg, msgParams);
		}
	}

	private String input;
	private int currPos;

	private int tokenStart;
	private int tokenEnd;
	private TokenType tokenType;
	private boolean tokenContainsEscapedChars;

	public ArgListTokenizer() {
		this(null);
	}

	public ArgListTokenizer(String input) {
		super();
		reset();
		this.input = input;
	}

	protected void reset() {
		currPos = 0;

		tokenStart = tokenEnd = -1;
		tokenType = null;
		tokenContainsEscapedChars = false;
	}

	/**
	 * Returns the next token that is <em>not</em> whitespace.
	 * 
	 * @return The type of the next token that is not {@code WHITE_SPACE}.
	 */
	public TokenType nextTokenNoWhitespace() {
		TokenType t;
		while ((t = nextToken()) == TokenType.WHITE_SPACE) {
			// do nothing
		}
		return t;
	}

	/**
	 * Returns the next token in the input String. This is on of the values of
	 * {@link TokenType}, or {@code null}, if the end of the input was reached.
	 * This method can throw the unchecked {@link ParseException}, if there was
	 * a problem splitting the input string in tokens.
	 * 
	 * @return The current token's type.
	 */
	public TokenType nextToken() {
		tokenStart = currPos;
		tokenContainsEscapedChars = false;

		// end of input?
		if (currPos >= input.length()) {
			tokenEnd = tokenStart = currPos;
			currPos++;
			tokenType = null;
			return null;
		}

		char c = input.charAt(currPos);
		currPos++;
		tokenEnd = currPos;

		switch (c) {
		case '=':
			tokenType = TokenType.EQUALS;
			break;
		case '(':
			tokenType = TokenType.PARENS_OPEN;
			break;
		case ')':
			tokenType = TokenType.PARENS_CLOSE;
			break;
		case ';':
			tokenType = TokenType.SEMICOLON;
			break;
		case ' ':
		case '\t':
		case '\r':
		case '\n':
			tokenType = TokenType.WHITE_SPACE;
			whiteSpace: while (currPos < input.length()) {
				switch (input.charAt(currPos)) {
				case ' ':
				case '\t':
				case '\r':
				case '\n':
					break; // switch
				default:
					// found end of whitespace
					tokenEnd = currPos;
					break whiteSpace;
				}

				currPos++;
			}
			break;
		default:
			tokenType = TokenType.STRING;
			readStringToken(c);
		}

		return tokenType;
	}

	private void readStringToken(char firstChar) {
		boolean isQuoted = false;
		if (firstChar == '"') {
			isQuoted = true;
		}

		boolean escape = firstChar == '\\';
		char c = 0;
		loop: while (currPos < input.length()) {
			c = input.charAt(currPos);
			if (escape) {
				c = 0;
				escape = false;
				tokenContainsEscapedChars = true;
			}

			if (isQuoted) {
				switch (c) {
				case '\\':
					escape = true;
					break;
				case '"':
					// found end of quoted String
					break loop;
				default: // do nothing
				}
			} else {
				switch (c) {
				case '\\':
					escape = true;
					break;
				case '"':// begin of new quoted String
				case '(':
				case ')':
				case ';':
				case '=':
				case ' ':
				case '\t':
				case '\r':
				case '\n':
					break loop;
				default: // do nothing
				}
			}

			currPos++;
		}

		if (escape)
			throw new ParseException(tokenStart,
					"Escape character at end of input.");

		if (isQuoted) {
			if (c == '"') {
				currPos++;
			} else {
				throw new ParseException(tokenStart,
						"Quoted string not closed.");
			}
		}
		tokenEnd = currPos;
	}

	/**
	 * Returns the portion of the input text that is associated with the current
	 * token. This method does not return surrounding quotes of a quoted
	 * {@code STRING} and unescapes any escaped characters.
	 * 
	 * @return The current token's text.
	 */
	public String currTokenText() {
		// was pushBackToken() called before
		if (currPos == tokenStart)
			throw new IllegalStateException();

		if (tokenType == null)
			return null; // end of input

		int start = tokenStart;
		int end = tokenEnd;
		if (input.charAt(start) == '"') {
			assert tokenType == TokenType.STRING;
			start++;
			end--;
			assert start <= end;
		}
		if (tokenContainsEscapedChars) {
			boolean escape = false;
			StringBuilder sb = new StringBuilder(end - start);
			for (int i = start; i < end; i++) {
				char c = input.charAt(i);
				if (escape) {
					sb.append(c);
					escape = false;
				} else {
					if (c != '\\') {
						sb.append(c);
					} else {
						escape = true;
					}
				}

			}

			return sb.toString();
		} else
			return input.substring(start, end);
	}

	public TokenType currTokenType() {
		// was pushBackToken() called before?
		if (currPos == tokenStart)
			throw new IllegalStateException();

		return tokenType;
	}

	public int currTokenStart() {
		// was pushBackToken() called before?
		if (currPos == tokenStart)
			throw new IllegalStateException();

		return tokenStart;
	}

	public int currTokenEnd() {
		// was pushBackToken() called before?
		if (currPos == tokenStart)
			throw new IllegalStateException();

		return tokenEnd;
	}

	/**
	 * Resets the current reading position back to beginning of the current
	 * token, so {@link #nextToken()} will see the same token again. This is
	 * useful, if a parser detects a token he can't handle but has to pass back
	 * to a parent parser for proper processing.
	 */
	public void pushBackToken() {
		if (currPos == tokenStart)
			throw new IllegalStateException(); // this works only once
		currPos = tokenStart;
	}

	/**
	 * Sets the input string to work on.
	 * 
	 * @param input
	 *            The input string.
	 */
	public void setInput(String input) {
		this.input = Objects.requireNonNull(input);
	}

	/**
	 * Checks whether the actual token's type matches a certain set of expected
	 * types. If the types do not match, then a {@link ParseException} is
	 * raised.
	 * 
	 * @param actual
	 *            The current token's type.
	 * @param expected
	 *            All token types that are currently valid.
	 * @throws ParseException
	 *             If {@code actual} if not contained in {@code expected}.
	 */
	public void assureTokenTypes(TokenType actual, TokenType... expected)
			throws ParseException {
		for (TokenType e : expected) {
			if (actual == e)
				return;
		}

		String msg = "expected one of: %s, but found: %s, '%s'";
		if (expected.length == 1)
			msg = "expected %s, but found: %s, '%s'";
		throw new ParseException(currTokenStart(), msg,
				Arrays.deepToString(expected), actual, currTokenText());
	}

}
