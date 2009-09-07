/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_TABLES_ICE
#define OMERO_TABLES_ICE

#include <omero/ModelF.ice>
#include <omero/RTypes.ice>
#include <omero/Collections.ice>
#include <omero/Repositories.ice>
#include <omero/ServerErrors.ice>

module omero {

    module grid {

    //
    // User-consumable types dealing with
    // measurements/results ("tables").
    // ========================================================================
    //

        class Column {

            string name;
            string description;

        };

        class FileColumn extends Column {
            omero::api::LongArray values;
        };

        class ImageColumn extends Column {
            omero::api::LongArray values;
        };

        class RoiColumn extends Column {
            omero::api::LongArray values;
        };

        class WellColumn extends Column {
            omero::api::LongArray values;
        };

        class BoolColumn extends Column {
            omero::api::BoolArray values;
        };

        class DoubleColumn extends Column {
            omero::api::DoubleArray values;
        };

        class LongColumn extends Column {
            omero::api::LongArray values;
        };

        class StringColumn extends Column {
            omero::api::StringArray values;
        };

        sequence<Column> ColumnArray;

        class Data {

            long lastModification;
            omero::api::LongArray rowNumbers;
            ColumnArray columns;

        };

        interface Table {


            //
            // Reading ======================================================
            //

            omero::model::OriginalFile
                getOriginalFile();

            bool
                isWrite();

            /**
             * Returns empty columns.
             **/
            ColumnArray
                getHeaders();

            long
                getNumberOfRows();

            /**
             * http://www.pytables.org/docs/manual/apb.html
             *
             * Leave all three of start, stop, step to 0 to disable.
             *
             * TODO:Test effect of returning a billion rows matching getWhereList()
             *
             **/
            omero::api::LongArray
                getWhereList(string condition, omero::RTypeDict variables, long start, long stop, long step);

            Data
                readCoordinates(omero::api::LongArray rowNumbers);


            //
            // Writing ========================================================
            //

            void
                addData(ColumnArray cols);

            //
            // Metadata =======================================================
            //

            omero::RTypeDict
                getAllMetadata();

            omero::RType
                getMetadata(string key);

            void
                setAllMetadata(omero::RTypeDict dict);

            void
                setMetadata(string key, omero::RType value);

            //
            // Life-cycle =====================================================
            //

            /**
             * Initializes the structure based on
             **/
            void
                initialize(ColumnArray cols);

            /**
             * Adds a column and returns the position index of the new column.
             **/
            int
                addColumn(Column col);

            /**
             **/
            void
                delete();

            /**
             **/
            void
                close();

        };


    //
    // Interfaces and types running the backend.
    // Used by OMERO.blitz to manage the public
    // omero.api types.
    // ========================================================================
    //

        interface Tables {

            /**
             * Returns the Repository which this Tables service is watching.
             **/
             omero::grid::Repository*
                getRepository();

            /**
             * Returns the Table service for the given "OMERO.tables" file.
             */
            Table*
                getTable(omero::model::OriginalFile file);

        };

    };


};

#endif
