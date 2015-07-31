/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
/*
 * AttachmentExportException.java
 *
 * Created on May 29, 2011, 12:00 PM by Edwin van der Wal
 */
package games.strategy.engine.data.export;

/**
 * Exception used by the AttachmentExporters
 *
 * @author Edwin van der Wal
 *
 */
public class AttachmentExportException extends Exception {
  private static final long serialVersionUID = 6134166689856636372L;

  public AttachmentExportException(final String error) {
    super(error);
  }
}
