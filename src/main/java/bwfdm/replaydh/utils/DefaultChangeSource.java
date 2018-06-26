/*
 * Unless expressly otherwise stated, code from this project is licensed under the MIT license [https://opensource.org/licenses/MIT].
 *
 * Copyright (c) <2018> <Markus Gärtner, Volodymyr Kushnarenko, Florian Fritze, Sibylle Hermann and Uli Hahn>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bwfdm.replaydh.utils;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultChangeSource implements ChangeSource {

	private final List<ChangeListener> listeners = new ArrayList<>();

	private transient ChangeEvent changeEvent;

	private final Object owner;

	public DefaultChangeSource(Object owner) {
		this.owner = requireNonNull(owner);
	}

	public DefaultChangeSource() {
		this.owner = this;
	}

	private ChangeEvent getChangeEvent() {
		if(changeEvent==null) {
			changeEvent = new ChangeEvent(owner);
		}
		return changeEvent;
	}

	/**
	 * @see bwfdm.replaydh.utils.ChangeSource#addChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * @see bwfdm.replaydh.utils.ChangeSource#removeChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void removeChangeListener(ChangeListener listener) {
		listeners.remove(listener);
	}

	public void fireChange() {
		if(!listeners.isEmpty()) {
			ChangeEvent event = getChangeEvent();
			for(ChangeListener listener : listeners) {
				listener.stateChanged(event);
			}
		}
	}
}
