package com.example.twapp

import android.content.Context
import android.content.Intent
import android.graphics.Outline
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.BaseAdapter
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.add_ticket.view.*
import kotlinx.android.synthetic.main.tweet_ticket.view.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var listTweets= arrayListOf<Ticket>()
    private var adapter:TweetAdapter?= null
    private var myEmail: String?= null
    private var userUID: String?= null
    private val storageRef = Firebase.storage.reference
    val database = Firebase.database.reference
    private var downloadURL: String?= null
    var name: String?= null
    private var uri: Uri?= null
    var tweetId: String?= null
    var counter: Int = 0
    val simpleDateFormat = SimpleDateFormat("ddMMyyyy,HH:mm:ss", Locale.forLanguageTag("tr-TR"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val b: Bundle = intent.extras!!
            myEmail = b.getString("email")
            userUID = b.getString("uid")

            adapter=TweetAdapter(this,listTweets)
            lvTweets.adapter=adapter
            loadPost()
    }

    inner class TweetAdapter:BaseAdapter{
        private var listNotesAdapter=ArrayList<Ticket>()
        private var context:Context?=null
        constructor(context: Context, listNotesAdapter: ArrayList<Ticket>):super(){
            this.listNotesAdapter=listNotesAdapter
            this.context=context
        }

        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
            val myTweet = listNotesAdapter[p0]

            val myView= layoutInflater.inflate(R.layout.add_ticket,null)
            if (myTweet.tweetPersonUID.equals("add")){

                myView.iv_attach.setOnClickListener{
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                }

                myView.iv_post.setOnClickListener {
                    val newItemRef = database.child("posts").push()
                    newItemRef.setValue(
                        PostInfo(
                            userUID!!,
                            myView.etPost.text.toString(),
                            "$downloadURL",
                            simpleDateFormat.format(Date()).toString()
                        )
                    ).addOnSuccessListener {
                        myView.etPost.setText("")
                        uri = null
                        downloadURL = null
                    }
                    database.child("likes").child(newItemRef.key.toString()).child("counter").setValue(0)
                }
                return myView
            }else {

                val myView = layoutInflater.inflate(R.layout.tweet_ticket, null)
                myView.txt_tweet.text = myTweet.tweetText
                Picasso.get().load(myTweet.tweetImageURL).into(myView.tweet_picture)
                myView.txt_tweet_date.text = myTweet.tweetDate
                tweetId = myTweet.tweetId.toString()
                myView.likeCount.text = likeCounter(tweetId!!,0).toString()

                val ref = database.child("likes").child(myTweet.tweetPersonUID!!)
                val valueTask = ref.get()
                valueTask.addOnSuccessListener { snapshot ->
                    if (snapshot.child(tweetId!!).child(userUID.toString()).exists()) {
                        myView.iv_like.tag = "selected"
                        myView.iv_like.setImageResource(android.R.drawable.btn_star_big_on)
                    }
                }.addOnFailureListener {
                    // Handle the error here.
                }

                myView.iv_like.setOnClickListener{
                    // Eğer resim tıklanmamışsa, resmi değiştirin ve tıklanmış olarak işaretleyin
                    if (myView.iv_like.tag == "selected") {
                        myView.likeCount.text = likeCounter(tweetId!!,1).toString()
                        // Resmi değiştirin (örneğin, "selected" durumunda gösterilecek bir resim yükleyin)
                        myView.iv_like.setImageResource(android.R.drawable.btn_star_big_off)
                        myView.iv_like.tag = "unselected"
                        val date = simpleDateFormat.format(Date())
                        database.child("likes").child(tweetId!!).child(userUID.toString()).setValue(date)
                    }
                    // Eğer resim zaten tıklanmışsa, resmi eski haline getirin ve tıklanmamış olarak işaretleyin
                    else {
                        myView.likeCount.text = likeCounter(tweetId!!,-1).toString()
                        // Resmi eski haline getirin (örneğin, "unselected" durumunda gösterilecek bir resim yükleyin)
                        myView.iv_like.setImageResource(android.R.drawable.btn_star_big_on)
                        myView.iv_like.tag = "selected"
                        database.child("likes").child(tweetId!!).child(userUID.toString()).removeValue()

                    }
                }

                database.child("users").child(myTweet.tweetPersonUID!!).addValueEventListener(object :ValueEventListener{

                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {

                            val td=snapshot.value as HashMap<*, *>
                            for (key in td.keys){
                                val userInfo = td[key] as String
                                if(key == "profileImageURL"){
                                    Picasso.get().load(userInfo).into(myView.picture_path)
                                    myView.picture_path.outlineProvider = object : ViewOutlineProvider() {
                                        override fun getOutline(view: View?, outline: Outline?) {
                                            outline?.setRoundRect(0, 0, view!!.width, view.height, view.width / 2F)
                                        }
                                    }
                                    myView.picture_path.clipToOutline = true
                                }else{
                                    myView.txtUserName.text = userInfo
                                }
                            }

                        }catch (e:java.lang.Exception){}
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })

                return myView

            }
        }

        override fun getCount(): Int {
            return listNotesAdapter.size
        }

        override fun getItem(p0: Int): Any {
            return listNotesAdapter[p0]
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

    }

    private fun uploadImage(path: String) {
        name = myEmail.toString().subSequence(0, 4).toString() + "." + simpleDateFormat.format(Date())
        val uploadTask = storageRef.child("$path/$name").putFile(uri!!)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            storageRef.child("$path/$name").downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                downloadURL = task.result.toString()
            }
        }
    }

    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { Uri ->
        uri = Uri
        uploadImage("postImage")
    }
    val pickMedia2 = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { Uri ->
        uri = Uri
        uploadImage("profilePic")
    }

    private fun loadPost(){
        database.child("posts").addValueEventListener(object :ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    listTweets.clear()
                    listTweets.add(Ticket("0","o","url","add","date"))


                    val td=snapshot.value as HashMap<String, Any>
                    for (key in td.keys){
                        val post = td[key] as HashMap<*, *>
                        listTweets.add(Ticket(
                            key,
                            post["text"] as String,
                            post["postImage"] as String,
                            post["userUID"] as String,
                            post["date"] as String))
                    }

                    adapter!!.notifyDataSetChanged()

                }catch (e:java.lang.Exception){}
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun likeCounter(tweetId: String, add: Int): Int {
        val ref = database.child("likes").child(tweetId).child("counter")

        val valueTask = ref.get()
        valueTask.addOnSuccessListener { snapshot ->
            counter = snapshot.getValue(Int::class.java)!!
            counter += add
            ref.setValue(counter)
        }.addOnFailureListener {
            // Handle the error here.
        }
        return counter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.navigation_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.nav_account -> {
                pickMedia2.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                database.child("users").child(userUID!!).child("profileImageURL").setValue(downloadURL).addOnSuccessListener{
                    downloadURL = null
                }
                true
            }
            R.id.nav_logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}