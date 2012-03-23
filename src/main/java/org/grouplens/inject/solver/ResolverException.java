/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.inject.solver;

/**
 * ResolverExceptions are thrown when a Resolver is unable to resolve a
 * dependency graph completely.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class ResolverException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ResolverException() {
        super();
    }

    public ResolverException(String msg) {
        super(msg);
    }

    public ResolverException(Throwable throwable) {
        super(throwable);
    }

    public ResolverException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}