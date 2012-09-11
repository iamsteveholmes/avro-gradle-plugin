package org.apache.avro.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class AvroPlugin implements Plugin<Project> {

    @Override
    void apply( final Project project ) {

        project.task( type: AvroCompileTask, 'compileAvro' )

        project.afterEvaluate {
            project.tasks.compileAvro.execute()
        }
    }
}
