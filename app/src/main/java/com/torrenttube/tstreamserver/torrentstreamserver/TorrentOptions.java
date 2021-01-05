package com.torrenttube.tstreamserver.torrentstreamserver;

import java.io.File;

public final class TorrentOptions {

    String saveLocation = "/";
    String proxyHost;
    String proxyUsername;
    String proxyPassword;
    String peerFingerprint;
    Integer maxDownloadSpeed = 0;
    Integer maxUploadSpeed = 0;
    Integer maxConnections = 200;
    Integer maxDht = 88;
    Integer listeningPort = -1;
    Boolean removeFiles = false;
    Boolean anonymousMode = false;
    Boolean autoDownload = true;
    Long prepareSize = 15 * 1024L * 1024L;

    private TorrentOptions() {
        // Unused
    }

    private TorrentOptions(TorrentOptions torrentOptions) {
        this.saveLocation = torrentOptions.saveLocation;
        this.proxyHost = torrentOptions.proxyHost;
        this.proxyUsername = torrentOptions.proxyUsername;
        this.proxyPassword = torrentOptions.proxyPassword;
        this.peerFingerprint = torrentOptions.peerFingerprint;
        this.maxDownloadSpeed = torrentOptions.maxDownloadSpeed;
        this.maxUploadSpeed = torrentOptions.maxUploadSpeed;
        this.maxConnections = torrentOptions.maxConnections;
        this.maxDht = torrentOptions.maxDht;
        this.listeningPort = torrentOptions.listeningPort;
        this.removeFiles = torrentOptions.removeFiles;
        this.anonymousMode = torrentOptions.anonymousMode;
        this.autoDownload = torrentOptions.autoDownload;
        this.prepareSize = torrentOptions.prepareSize;
    }

    public TorrentOptions.Builder toBuilder() {
        return new TorrentOptions.Builder(this);
    }

    public static class Builder {

        private TorrentOptions torrentOptions;

        public Builder() {
            torrentOptions = new TorrentOptions();
        }

        private Builder(TorrentOptions torrentOptions) {
            torrentOptions = new TorrentOptions(torrentOptions);
        }

        public TorrentOptions.Builder saveLocation(String saveLocation) {
            torrentOptions.saveLocation = saveLocation;
            return this;
        }

        public TorrentOptions.Builder saveLocation(File saveLocation) {
            torrentOptions.saveLocation = saveLocation.getAbsolutePath();
            return this;
        }

        public TorrentOptions.Builder maxUploadSpeed(Integer maxUploadSpeed) {
            torrentOptions.maxUploadSpeed = maxUploadSpeed;
            return this;
        }

        public TorrentOptions.Builder maxDownloadSpeed(Integer maxDownloadSpeed) {
            torrentOptions.maxDownloadSpeed = maxDownloadSpeed;
            return this;
        }

        public TorrentOptions.Builder maxConnections(Integer maxConnections) {
            torrentOptions.maxConnections = maxConnections;
            return this;
        }

        public TorrentOptions.Builder maxActiveDHT(Integer maxActiveDHT) {
            torrentOptions.maxDht = maxActiveDHT;
            return this;
        }

        public TorrentOptions.Builder removeFilesAfterStop(Boolean b) {
            torrentOptions.removeFiles = b;
            return this;
        }

        public TorrentOptions.Builder prepareSize(Long prepareSize) {
            torrentOptions.prepareSize = prepareSize;
            return this;
        }

        public TorrentOptions.Builder listeningPort(Integer port) {
            torrentOptions.listeningPort = port;
            return this;
        }

        public TorrentOptions.Builder proxy(String host, String username, String password) {
            torrentOptions.proxyHost = host;
            torrentOptions.proxyUsername = username;
            torrentOptions.proxyPassword = password;
            return this;
        }

        public TorrentOptions.Builder peerFingerprint(String peerId) {
            torrentOptions.peerFingerprint = peerId;
            torrentOptions.anonymousMode = false;
            return this;
        }

        public TorrentOptions.Builder anonymousMode(Boolean enable) {
            torrentOptions.anonymousMode = enable;
            if (torrentOptions.anonymousMode)
                torrentOptions.peerFingerprint = null;
            return this;
        }

        public TorrentOptions.Builder autoDownload(Boolean enable) {
            torrentOptions.autoDownload = enable;
            return this;
        }

        public TorrentOptions build() {
            return torrentOptions;
        }

    }

}