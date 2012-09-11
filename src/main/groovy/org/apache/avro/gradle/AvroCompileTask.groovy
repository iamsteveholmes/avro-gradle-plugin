package org.apache.avro.gradle

import org.apache.avro.compiler.idl.Idl
import org.apache.avro.compiler.idl.ParseException
import org.apache.avro.compiler.specific.SpecificCompiler
import org.apache.avro.generic.GenericData
import org.apache.maven.artifact.DependencyResolutionRequiredException
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction

class AvroCompileTask extends SourceTask {

    static final String IDL_EXTENSION = ".avdl"
    static final String PROTOCOL_EXTENSION = ".avpr"
    static final String SCHEMA_EXTENSION = ".avsc"

    String stringType = "CharSequence"
    String templateDirectory = "/org/apache/avro/compiler/specific/templates/java/classic/"
    File destinationDir

    AvroCompileTask( ) {
        super()
        include "**/*$PROTOCOL_EXTENSION", "**/*$SCHEMA_EXTENSION", "**/*$IDL_EXTENSION"
    }

    @TaskAction
    void compile( ) {

        if ( source.empty ) {
            throw new Exception( "source is empty" )
        }

        source.each { File file ->

            logger.info( "Processing ${ file.name }" + file.name )
            try {

                // First check if GenAvro needs to be run
                if ( file.absolutePath.endsWith( IDL_EXTENSION ) ) {

                    ClassLoader loader = new URLClassLoader( project.configurations.runtime.collect {
                        it.toURI().toURL()
                    } as URL[] )

                    Idl parser = new Idl( file, loader )
                    Protocol p = parser.CompilationUnit()
                    String json = p.toString( true )
                    Protocol protocol = Protocol.parse( json )
                    SpecificCompiler compiler = new SpecificCompiler( protocol )
                    compiler.setStringType( GenericData.StringType.valueOf( stringType ) )
                    compiler.setTemplateDir( templateDirectory )
                    compiler.compileToDestination( null, destinationDir )

                } else if ( file.name.endsWith( SCHEMA_EXTENSION ) ) {

                    SpecificCompiler.compileSchema( file, destinationDir )

                } else if ( file.name.endsWith( PROTOCOL_EXTENSION ) ) {

                    SpecificCompiler.compileProtocol( file, destinationDir )

                } else {

                    throw new Exception( "Do not know file type of ${ file.name }" )
                }
            } catch ( ParseException e ) {
                throw new IOException( e )
            } catch ( DependencyResolutionRequiredException e ) {
                throw new IOException( e )
            }
        }
    }
}
