package external.vimeo

import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.test.{PlayRunners, FakeApplication}

/**
  * author: cvandrei
  * since: 2016-02-03
  */
class VideoStatusUtilSpec extends Specification with PlayRunners {

  val webjazzJson = Json.parse(
    """{
       |    "uri": "/videos/152690945",
       |    "name": "MV1_KW3_Do",
       |    "description": "no description",
       |    "link": "https://vimeo.com/152690945",
       |    "duration": 587,
       |    "width": 1280,
       |    "language": null,
       |    "height": 720,
       |    "embed": {
       |        "uri": null,
       |        "html": "<iframe src=\"https://player.vimeo.com/video/152690945?badge=0&autopause=0&player_id=0\" width=\"1280\" height=\"720\"frameborder=\"0\" title=\"MV1_KW3_Do\" webkitallowfullscreen mozallowfullscreen allowfullscreen></iframe>",
       |        "buttons": {
       |            "like": true,
       |            "watchlater": true,
       |            "share": true,
       |            "embed": true,
       |            "hd": false,
       |            "fullscreen": true,
       |            "scaling": true
       |        },
       |        "logos": {
       |            "vimeo": true,
       |            "custom": {
       |                "active": false,
       |                "link": null,
       |                "sticky": false
       |            }
       |        },
       |        "title": {
       |            "name": "user",
       |            "owner": "user",
       |            "portrait": "user"
       |        },
       |        "playbar": true,
       |        "volume": true,
       |        "color": "00adef"
       |    },
       |    "created_time": "2016-01-22T11:52:30+00:00",
       |    "modified_time": "2016-01-22T11:56:38+00:00",
       |    "content_rating": [
       |        "safe"
       |    ],
       |    "license": "by-nc-nd",
       |    "privacy": {
       |        "view": "nobody",
       |        "embed": "public",
       |        "download": false,
       |        "add": true,
       |        "comments": "anybody"
       |    },
       |    "pictures": {
       |        "uri": "/videos/152690945/pictures/552752804",
       |        "active": true,
       |        "type": "custom",
       |        "sizes": [
       |            {
       |                "width": 100,
       |                "height": 75,
       |                "link": "https://i.vimeocdn.com/video/552752804_100x75.jpg?r=pad"
       |            },
       |            {
       |                "width": 200,
       |                "height": 150,
       |                "link": "https://i.vimeocdn.com/video/552752804_200x150.jpg?r=pad"
       |            },
       |            {
       |                "width": 295,
       |                "height": 166,
       |                "link": "https://i.vimeocdn.com/video/552752804_295x166.jpg?r=pad"
       |            },
       |            {
       |                "width": 640,
       |                "height": 360,
       |                "link": "https://i.vimeocdn.com/video/552752804_640x360.jpg?r=pad"
       |            },
       |            {
       |                "width": 960,
       |                "height": 540,
       |                "link": "https://i.vimeocdn.com/video/552752804_960x540.jpg?r=pad"
       |            },
       |            {
       |                "width": 1280,
       |                "height": 720,
       |                "link": "https://i.vimeocdn.com/video/552752804_1280x720.jpg?r=pad"
       |            }
       |        ]
       |    },
       |    "tags": [],
       |    "stats": {
       |        "plays": 0
       |    },
       |    "metadata": {
       |        "connections": {
       |            "comments": {
       |                "uri": "/videos/152690945/comments",
       |                "options": [
       |                    "GET",
       |                    "POST"
       |                ],
       |                "total": 0
       |            },
       |            "credits": {
       |                "uri": "/videos/152690945/credits",
       |                "options": [
       |                    "GET",
       |                    "POST"
       |                ],
       |                "total": 1
       |            },
       |            "likes": {
       |                "uri": "/videos/152690945/likes",
       |                "options": [
       |                    "GET"
       |                ],
       |                "total": 0
       |            },
       |            "pictures": {
       |                "uri": "/videos/152690945/pictures",
       |                "options": [
       |                    "GET",
       |                    "POST"
       |                ],
       |                "total": 1
       |            },
       |            "texttracks": {
       |                "uri": "/videos/152690945/texttracks",
       |                "options": [
       |                    "GET",
       |                    "POST"
       |                ],
       |                "total": 0
       |            },
       |            "related": null
       |        },
       |        "interactions": {
       |            "watchlater": {
       |                "added": false,
       |                "added_time": null,
       |                "uri": "/users/26763335/watchlater/152690945"
       |            }
       |        }
       |    },
       |    "user": {
       |        "uri": "/users/26763335",
       |        "name": "indarium",
       |        "link": "https://vimeo.com/indariummedia",
       |        "location": "Berlin",
       |        "bio": null,
       |        "created_time": "2014-04-09T08:41:34+00:00",
       |        "account": "pro",
       |        "pictures": {
       |            "uri": "/users/26763335/pictures/7781902",
       |            "active": true,
       |            "type": "custom",
       |            "sizes": [
       |                {
       |                    "width": 30,
       |                    "height": 30,
       |                    "link": "https://i.vimeocdn.com/portrait/7781902_30x30.jpg?r=pad"
       |                },
       |                {
       |                    "width": 75,
       |                    "height": 75,
       |                    "link": "https://i.vimeocdn.com/portrait/7781902_75x75.jpg?r=pad"
       |                },
       |                {
       |                    "width": 100,
       |                    "height": 100,
       |                    "link": "https://i.vimeocdn.com/portrait/7781902_100x100.jpg?r=pad"
       |                },
       |                {
       |                    "width": 300,
       |                    "height": 300,
       |                    "link": "https://i.vimeocdn.com/portrait/7781902_300x300.jpg?r=pad"
       |                }
       |            ]
       |        },
       |        "websites": [],
       |        "metadata": {
       |            "connections": {
       |                "activities": {
       |                    "uri": "/users/26763335/activities",
       |                    "options": [
       |                        "GET"
       |                    ]
       |                },
       |                "albums": {
       |                    "uri": "/users/26763335/albums",
       |                    "options": [
       |                        "GET"
       |                    ],
       |                    "total": 1
       |                },
       |                "channels": {
       |                    "uri": "/users/26763335/channels",
       |                    "options": [
       |                        "GET"
       |                    ],
       |                    "total": 4
       |                },
       |                "feed": {
       |                    "uri": "/users/26763335/feed",
       |                    "options": [
       |                        "GET"
       |                    ]
       |                },
       |                "followers": {
       |                    "uri": "/users/26763335/followers",
       |                    "options": [
       |                        "GET"
       |                    ],
       |                    "total": 0
       |                },
       |                "following": {
       |                    "uri": "/users/26763335/following",
       |                    "options": [
       |                        "GET"
       |                    ],
       |                    "total": 0
       |                },
       |                "groups": {
       |                    "uri": "/users/26763335/groups",
       |                    "options": [
       |                        "GET"
       |                    ],
       |                    "total": 3
       |                },
       |                "likes": {
       |                    "uri": "/users/26763335/likes",
       |                    "options": [
       |                        "GET"
       |                    ],
       |                    "total": 0
       |                },
       |                "portfolios": {
       |                    "uri": "/users/26763335/portfolios",
       |                    "options": [
       |                        "GET"
       |                    ],
       |                    "total": 0
       |                },
       |                "videos": {
       |                    "uri": "/users/26763335/videos",
       |                    "options": [
       |                        "GET"
       |                    ],
       |                    "total": 17
       |                },
       |                "watchlater": {
       |                    "uri": "/users/26763335/watchlater",
       |                    "options": [
       |                        "GET"
       |                    ],
       |                    "total": 0
       |                },
       |                "shared": {
       |                    "uri": "/users/26763335/shared/videos",
       |                    "options": [
       |                        "GET"
       |                    ],
       |                    "total": 0
       |                },
       |                "pictures": {
       |                    "uri": "/users/26763335/pictures",
       |                    "options": [
       |                        "GET",
       |                        "POST"
       |                    ],
       |                    "total": 2
       |                }
       |            }
       |        },
       |        "preferences": {
       |            "videos": {
       |                "privacy": "nobody"
       |            }
       |        },
       |        "content_filter": [
       |            "language",
       |            "drugs",
       |            "violence",
       |            "nudity",
       |            "safe",
       |            "unrated"
       |        ],
       |        "upload_quota": {
       |            "space": {
       |                "free": 21301274339,
       |                "max": 21474836480,
       |                "used": 173562141
       |            },
       |            "quota": {
       |                "hd": true,
       |                "sd": true
       |            }
       |        }
       |    },
       |    "download": [
       |        {
       |            "quality": "hd",
       |            "type": "video/mp4",
       |            "width": 1280,
       |            "height": 720,
       |            "expires": "2016-01-22T15:13:33+00:00",
       |            "link": "https://vimeo.com/api/file/download?clip_id=152690945&id=393716837&profile=113&codec=H264&exp=1453475613&sig=db6c87e0c3e2ea7706c39044beffc9f3fe666552",
       |            "created_time": "2016-01-22T11:52:48+00:00",
       |            "fps": 25,
       |            "size": 120692533,
       |            "md5": "b2b3412e3d757943f58d661928ff81bc"
       |        },
       |        {
       |            "quality": "sd",
       |            "type": "video/mp4",
       |            "width": 640,
       |            "height": 360,
       |            "expires": "2016-01-22T15:13:33+00:00",
       |            "link": "https://vimeo.com/api/file/download?clip_id=152690945&id=393716837&profile=112&codec=H264&exp=1453475613&sig=9da3b95451d97c0c6068b921d74b3a57c0d3679b",
       |            "created_time": "2016-01-22T11:52:48+00:00",
       |            "fps": 25,
       |            "size": 46921596,
       |            "md5": "7e514024821f9c730626a7b745a890d5"
       |        },
       |        {
       |            "quality": "sd",
       |            "type": "video/mp4",
       |            "width": 960,
       |            "height": 540,
       |            "expires": "2016-01-22T15:13:33+00:00",
       |            "link": "https://vimeo.com/api/file/download?clip_id=152690945&id=393716837&profile=165&codec=H264&exp=1453475613&sig=5fb1cfcd439a7017e01c1616a63f539026df982a",
       |            "created_time": "2016-01-22T11:52:48+00:00",
       |            "fps": 25,
       |            "size": 81955688,
       |            "md5": "07bb12dff52ffe8b46ecc84cbe501e2f"
       |        },
       |        {
       |            "quality": "source",
       |            "type": "source",
       |            "width": 1280,
       |            "height": 720,
       |            "expires": "2016-01-22T15:13:33+00:00",
       |            "link": "https://vimeo.com/api/file/download?clip_id=152690945&id=393716837&profile=source&codec=source&exp=1453475613&sig=6edb096abeedb91006dffc7080b3c4ce1f0ff06d",
       |            "created_time": "2016-01-22T11:50:53+00:00",
       |            "fps": 25,
       |            "size": 105499819,
       |            "md5": "ccdb1589199b72cdcfe05ad5e5c9b6a4"
       |        }
       |    ],
       |    "files": [
       |        {
       |            "quality": "hd",
       |            "type": "video/mp4",
       |            "width": 1280,
       |            "height": 720,
       |            "link": "http://player.vimeo.com/external/152690945.hd.mp4?s=e514c83b1988801c9067e150d2470e32bfc1c2c0&profile_id=113&oauth2_token_id=393716837",
       |            "link_secure": "https://player.vimeo.com/external/152690945.hd.mp4?s=e514c83b1988801c9067e150d2470e32bfc1c2c0&profile_id=113&oauth2_token_id=393716837",
       |            "created_time": "2016-01-22T11:52:48+00:00",
       |            "fps": 25,
       |            "size": 120692533,
       |            "md5": "b2b3412e3d757943f58d661928ff81bc"
       |        },
       |        {
       |            "quality": "sd",
       |            "type": "video/mp4",
       |            "width": 640,
       |            "height": 360,
       |            "link": "http://player.vimeo.com/external/152690945.sd.mp4?s=4e407fbffe3eaac65c8e8c7907dcf1146959f786&profile_id=112&oauth2_token_id=393716837",
       |            "link_secure": "https://player.vimeo.com/external/152690945.sd.mp4?s=4e407fbffe3eaac65c8e8c7907dcf1146959f786&profile_id=112&oauth2_token_id=393716837",
       |            "created_time": "2016-01-22T11:52:48+00:00",
       |            "fps": 25,
       |            "size": 46921596,
       |            "md5": "7e514024821f9c730626a7b745a890d5"
       |        },
       |        {
       |            "quality": "sd",
       |            "type": "video/mp4",
       |            "width": 960,
       |            "height": 540,
       |            "link": "http://player.vimeo.com/external/152690945.sd.mp4?s=4e407fbffe3eaac65c8e8c7907dcf1146959f786&profile_id=165&oauth2_token_id=393716837",
       |            "link_secure": "https://player.vimeo.com/external/152690945.sd.mp4?s=4e407fbffe3eaac65c8e8c7907dcf1146959f786&profile_id=165&oauth2_token_id=393716837",
       |            "created_time": "2016-01-22T11:52:48+00:00",
       |            "fps": 25,
       |            "size": 81955688,
       |            "md5": "07bb12dff52ffe8b46ecc84cbe501e2f"
       |        },
       |        {
       |            "quality": "hls",
       |            "type": "video/mp4",
       |            "link": "https://player.vimeo.com/external/152690945.m3u8?p=mobile,standard,high&s=ad2d7699cc43e7a3573328d2a6d6442d621ab9f7&oauth2_token_id=393716837",
       |            "link_secure": "https://player.vimeo.com/external/152690945.m3u8?p=mobile,standard,high&s=ad2d7699cc43e7a3573328d2a6d6442d621ab9f7&oauth2_token_id=393716837",
       |            "created_time": "2016-01-22T11:52:48+00:00",
       |            "fps": 25,
       |            "size": 120692533,
       |            "md5": "b2b3412e3d757943f58d661928ff81bc"
       |        }
       |    ],
       |    "app": {
       |        "name": "mabb/mmv WebTV",
       |        "uri": "/apps/48257"
       |    },
       |    "status": "available",
       |    "embed_presets": null
       |}
    """.stripMargin)

  "The Json library" should {

    "extract /pictures from Json and convert it to object Pictures" in {
      running(FakeApplication()) {

        // test
        val pictures = VideoStatusUtil.extractPictures(webjazzJson)

        // verify
        pictures.sizes must have size 6

      }
    }

    "extract /download from Json and convert it to List[Download]" in {
      running(FakeApplication()) {

        // test
        val downloads = VideoStatusUtil.extractDownloads(webjazzJson)

        // verify
        downloads must have size 4

      }
    }

    "extract /file from Json and convert it to List[File]" in {
      running(FakeApplication()) {

        // test
        val files = VideoStatusUtil.extractFiles(webjazzJson)

        // verify
        files must have size 4

      }
    }

  }

}
