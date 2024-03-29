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

import jasima.core.experiment.Experiment;

import java.io.File;
import java.util.Map;

/**
 * Saves (final) experiment results in an XML file.
 * 
 * @author Robin Kreis
 * @author Torsten Hildebrandt, 2013-01-08
 * @version 
 *          "$Id$"
 */
public class XmlSaver extends AbstractResultSaver {

	private static final long serialVersionUID = -7598515231019675606L;

	@Override
	protected void finished(Experiment e, Map<String, Object> results) {
		XmlUtil.saveXML(FileFormat.XSTREAM, results, new File(getActualResultBaseName() + ".xml"));
	}

	@Override
	public boolean checkBaseName(String base) {
		if (new File(base + ".xml").exists())
			return false;
		return true;
	}

}
