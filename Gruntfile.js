module.exports = function(grunt){

    "use strict";

    require('time-grunt')(grunt);
    require('jit-grunt')(grunt);

    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        concat: {
            options: {
                separator: ';'
            },
            dist: {
                src: [
                    'public/js/**/*.js'
                ],
                dest: 'public/js/scripts.min.js'
            }
        },
        uglify: {
            options: {
                banner: '/*! <%= pkg.name %> <%= grunt.template.today("dd-mm-yyyy") %> */\n'
            },
            dist: {
                files: {
                    'public/js/scripts.min.js': ['<%= concat.dist.dest %>']
                }
            }
        },
        cssmin: {
            dist: {
                files: {
                    'public/css/styles.min.css' : ['public/css/**/*.css']
                }
            }
        },
        svgmin: {
            dist: {
                files: [{
                    expand: true,
                    cwd: 'src/svg',
                    src: ['*.svg'],
                    dest: 'src/svg'
                }]
            }
        }
    });

    //TODO: creating appropiate HTML file
    grunt.registerTask('production', [
        'cssmin',
        'concat',
        'uglify'
    ]);

    grunt.registerTask('development', [
        'connect:serverLive',
        'watch'
    ]);
};
