wasd, service↔machine discovery
===============================


Usage
-----

- This service ingests Cassandra rings and DNS records across multiple sites at a regular interval, then exposes information about hosts and services.

- One can ask what a host does, optionally for which sites; which hosts in a site offer a service; which hosts offer a service for a site.

- Queries follow a simple REST API documented in `API`. Answers are serialized in JSON.

- The configuration is handled by typesafe's config; we recommend putting an `application.conf` in the classpath of the service.


Status
------

- Our command line clients are not ready for public distribution.

- Public documentation is lacking.

- Patches are welcome.


Example configuration
---------------------

    Server { RefreshRate: 60s }
    Sites {
      'london.example.com.' {
        Aliases = [ 'lon', 'london', 'london.example.com' ]
      }
      'paris.example.com.' {
        Aliases = [ 'par', 'paris', 'paris.example.com' ]
      }
    }
    Services {
      puppet {
        Records = [
          {
            Type: 'A', // round-robin
            Name: 'puppet',
          }
        ]
      }
      user_db {
        Records = [
          {Type: 'CNAME', Name: '_user_db_master._tcp'},
          {Type: 'CNAME', Name: '_user_db_slave._tcp'}
        ]
      }
      user_contents {Cassandra = [{Type: SRV, Name: _user_contents._cassandra}]}
    }