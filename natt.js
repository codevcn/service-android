{
  "info": {
    "name": "Task Manager API",
    "description": "API documentation for Task Manager application",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Authentication",
      "item": [
        {
          "name": "Register",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n    \"username\": \"testuser\",\n    \"email\": \"test@example.com\",\n    \"password\": \"password123\"\n}"
            },
            "url": {
              "raw": "http://localhost:8080/api/auth/register",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "auth", "register"]
            }
          }
        },
        {
          "name": "Login",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n    \"username\": \"testuser\",\n    \"password\": \"password123\"\n}"
            },
            "url": {
              "raw": "http://localhost:8080/api/auth/login",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "auth", "login"]
            }
          }
        }
      ]
    },
    {
      "name": "Tasks",
      "item": [
        {
          "name": "Get All Tasks",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "http://localhost:8080/api/tasks",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "tasks"]
            }
          }
        },
        {
          "name": "Get Task by ID",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "http://localhost:8080/api/tasks/1",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "tasks", "1"]
            }
          }
        },
        {
          "name": "Create Task",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n    \"taskName\": \"New Task\",\n    \"description\": \"Task description\",\n    \"phaseId\": 1\n}"
            },
            "url": {
              "raw": "http://localhost:8080/api/tasks",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "tasks"]
            }
          }
        },
        {
          "name": "Update Task",
          "request": {
            "method": "PUT",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n    \"taskName\": \"Updated Task\",\n    \"description\": \"Updated description\",\n    \"phaseId\": 1\n}"
            },
            "url": {
              "raw": "http://localhost:8080/api/tasks/1",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "tasks", "1"]
            }
          }
        },
        {
          "name": "Delete Task",
          "request": {
            "method": "DELETE",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "http://localhost:8080/api/tasks/1",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "tasks", "1"]
            }
          }
        }
      ]
    },
    {
      "name": "Projects",
      "item": [
        {
          "name": "Get All Projects",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "http://localhost:8080/api/projects",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "projects"]
            }
          }
        },
        {
          "name": "Get Project by ID",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "http://localhost:8080/api/projects/1",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "projects", "1"]
            }
          }
        },
        {
          "name": "Create Project",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n    \"projectName\": \"New Project\",\n    \"description\": \"Project description\"\n}"
            },
            "url": {
              "raw": "http://localhost:8080/api/projects",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "projects"]
            }
          }
        },
        {
          "name": "Update Project",
          "request": {
            "method": "PUT",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n    \"projectName\": \"Updated Project\",\n    \"description\": \"Updated description\"\n}"
            },
            "url": {
              "raw": "http://localhost:8080/api/projects/1",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "projects", "1"]
            }
          }
        },
        {
          "name": "Delete Project",
          "request": {
            "method": "DELETE",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "http://localhost:8080/api/projects/1",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "projects", "1"]
            }
          }
        }
      ]
    },
    {
      "name": "Comments",
      "item": [
        {
          "name": "Get All Comments",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "http://localhost:8080/api/comments",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "comments"]
            }
          }
        },
        {
          "name": "Get Comments by Task",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "http://localhost:8080/api/comments/task/1",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "comments", "task", "1"]
            }
          }
        },
        {
          "name": "Get Comment by ID",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "http://localhost:8080/api/comments/1",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "comments", "1"]
            }
          }
        },
        {
          "name": "Create Comment",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n    \"content\": \"New comment\",\n    \"taskId\": 1\n}"
            },
            "url": {
              "raw": "http://localhost:8080/api/comments",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "comments"]
            }
          }
        },
        {
          "name": "Update Comment",
          "request": {
            "method": "PUT",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n    \"content\": \"Updated comment\"\n}"
            },
            "url": {
              "raw": "http://localhost:8080/api/comments/1",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "comments", "1"]
            }
          }
        },
        {
          "name": "Delete Comment",
          "request": {
            "method": "DELETE",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "http://localhost:8080/api/comments/1",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "comments", "1"]
            }
          }
        }
      ]
    },
    {
      "name": "Files",
      "item": [
        {
          "name": "Get All Files",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "http://localhost:8080/api/files",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "files"]
            }
          }
        },
        {
          "name": "Get Files by Task",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "http://localhost:8080/api/files/task/1",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "files", "task", "1"]
            }
          }
        },
        {
          "name": "Get File by ID",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "http://localhost:8080/api/files/1",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "files", "1"]
            }
          }
        },
        {
          "name": "Upload File",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "body": {
              "mode": "formdata",
              "formdata": [
                {
                  "key": "file",
                  "type": "file",
                  "src": "/path/to/file"
                },
                {
                  "key": "taskId",
                  "value": "1",
                  "type": "text"
                }
              ]
            },
            "url": {
              "raw": "http://localhost:8080/api/files/upload",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "files", "upload"]
            }
          }
        },
        {
          "name": "Delete File",
          "request": {
            "method": "DELETE",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "http://localhost:8080/api/files/1",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "files", "1"]
            }
          }
        }
      ]
    },
    {
      "name": "Phases",
      "item": [
        {
          "name": "Get All Phases",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "http://localhost:8080/api/phases",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "phases"]
            }
          }
        },
        {
          "name": "Get Phase by ID",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "http://localhost:8080/api/phases/1",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "phases", "1"]
            }
          }
        },
        {
          "name": "Create Phase",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n    \"phaseName\": \"New Phase\",\n    \"description\": \"Phase description\",\n    \"projectId\": 1\n}"
            },
            "url": {
              "raw": "http://localhost:8080/api/phases",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "phases"]
            }
          }
        },
        {
          "name": "Update Phase",
          "request": {
            "method": "PUT",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n    \"phaseName\": \"Updated Phase\",\n    \"description\": \"Updated description\",\n    \"projectId\": 1\n}"
            },
            "url": {
              "raw": "http://localhost:8080/api/phases/1",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "phases", "1"]
            }
          }
        },
        {
          "name": "Delete Phase",
          "request": {
            "method": "DELETE",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "http://localhost:8080/api/phases/1",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "phases", "1"]
            }
          }
        }
      ]
    }
  ],
  "variable": [
    {
      "key": "token",
      "value": "your_jwt_token_here"
    }
  ]
} 